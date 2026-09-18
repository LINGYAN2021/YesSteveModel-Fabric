package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.natives.render.VertexFormatType;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

/**
 * Iris 兼容层。
 *
 * <p>开启光影后，实体类 RenderType 的顶点格式会被换成 Iris 的扩展格式
 * （{@code IrisVertexFormats.ENTITY}）：在普通 ENTITY 顶点之后追加
 * entity_id / mid_tex_uv / tangent 三段数据，顶点字节数因此变长。
 * 原生渲染器必须知道用哪种布局去解释这段缓冲。
 *
 * <p>若识别不出格式，{@code NativeRenderer.setupVertexConsumer} 只能退回
 * {@code FALLBACK} 布局，表现为开光影后模型渲染异常——这正是本类存在的意义。
 *
 * <p>Iris 是可选依赖，全部走反射，不给构建引入编译期依赖；未安装时
 * {@link #isInstalled()} 为 false，所有调用点退化到无光影时的行为。
 */
public class IrisCompat {
    private static final String MOD_ID = "iris";

    /**
     * Iris 的 mod 包名。1.7.0 之前是 coderbot 的包名，之后迁到 irisshaders
     */
    private static final String MODERN_PACKAGE = "net.irisshaders.iris.";
    private static final String LEGACY_PACKAGE = "net.coderbot.iris.";

    /**
     * 顶点格式同一性比较的基准：IrisVertexFormats.ENTITY
     */
    private static VertexFormat irisEntityFormat;

    /**
     * IrisApi 实例上的 isRenderingShadowPass()
     */
    private static MethodHandle shadowPassGetter;

    /**
     * CapturedRenderingState.INSTANCE 上的三个 getCurrentRendered*()
     */
    private static MethodHandle renderedEntityGetter;
    private static MethodHandle renderedBlockEntityGetter;
    private static MethodHandle renderedItemGetter;

    private static boolean INSTALLED = false;

    /**
     * 不识别的顶点大小只报告一次，避免每帧刷屏
     */
    private static boolean reportedUnknownSize = false;

    public static void init() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) {
            return;
        }

        var lookup = MethodHandles.lookup();
        try {
            irisEntityFormat = resolveEntityFormat();
            if (irisEntityFormat == null) {
                YesSteveModel.LOGGER.warn("Iris is present but IrisVertexFormats.ENTITY was not found; "
                        + "Iris-specific vertex layouts stay disabled");
                return;
            }

            var irisApiClass = resolveClass("api.v0.IrisApi");
            if (irisApiClass == null) {
                YesSteveModel.LOGGER.warn("Iris is present but IrisApi was not found; "
                        + "Iris-specific vertex layouts stay disabled");
                return;
            }
            var irisApi = lookup.findStatic(irisApiClass, "getInstance",
                    MethodType.methodType(irisApiClass)).invoke();
            shadowPassGetter = lookup.findVirtual(irisApiClass, "isRenderingShadowPass",
                    MethodType.methodType(boolean.class)).bindTo(irisApi);

            if (!resolveCapturedRenderingState(lookup)) {
                YesSteveModel.LOGGER.warn("Iris is present but CapturedRenderingState was not usable; "
                        + "shader entity ids will be reported as 0");
            }

            // 提前调用一次，签名不匹配的话在这里就暴露，而不是等到渲染线程上
            shadowPassGetter.invoke();
            getEntityId();

            INSTALLED = true;
            YesSteveModel.LOGGER.info("Iris compat enabled: entityFormatVertexSize={}, shadowPass={}, entityId={}",
                    irisEntityFormat.getVertexSize(),
                    shadowPassGetter.invoke(),
                    getEntityId());
        } catch (Throwable e) {
            YesSteveModel.LOGGER.error("Failed to set up Iris compat, falling back to non-shader rendering", e);
            irisEntityFormat = null;
            shadowPassGetter = null;
            renderedEntityGetter = null;
            renderedBlockEntityGetter = null;
            renderedItemGetter = null;
            INSTALLED = false;
        }
    }

    /**
     * 把运行时顶点格式映射到原生层支持的布局。
     *
     * <p>Iris 在不同版本里给 ENTITY 追加的数据不同，字节数随之不同，所以按顶点大小分派
     * （与旧版 YSM 一致）：56 是 entity_id + mid_tex_uv + tangent，55 / 54 是更早的布局。
     */
    public static Optional<VertexFormatType> determineVertexFormatType(VertexFormat vertexFormat) {
        if (!INSTALLED || vertexFormat != irisEntityFormat) {
            return Optional.empty();
        }

        return switch (vertexFormat.getVertexSize()) {
            case 56 -> Optional.of(ARCompat.isInstalled() ? VertexFormatType.IRIS_56_AR : VertexFormatType.IRIS_56);
            case 55 -> Optional.of(VertexFormatType.IRIS_55);
            case 54 -> Optional.of(VertexFormatType.IRIS_54);
            default -> {
                reportUnknownSize(vertexFormat);
                yield Optional.empty();
            }
        };
    }

    public static boolean isInstalled() {
        return INSTALLED;
    }

    public static boolean isRenderingShadow() {
        if (!INSTALLED) {
            return false;
        }
        try {
            return (boolean) shadowPassGetter.invoke();
        } catch (Throwable e) {
            YesSteveModel.LOGGER.error("Failed to read Iris shadow pass state", e);
            return false;
        }
    }

    /**
     * Iris 的 entity_id 顶点属性由三个 16 位值拼成（实体、方块实体、物品），
     * 低 6 字节有效；原生层按同样的顺序写入顶点。
     */
    public static long getEntityId() {
        if (!INSTALLED || renderedEntityGetter == null) {
            return 0L;
        }
        try {
            long entity = (int) renderedEntityGetter.invoke() & 0xFFFFL;
            long blockEntity = (int) renderedBlockEntityGetter.invoke() & 0xFFFFL;
            long item = (int) renderedItemGetter.invoke() & 0xFFFFL;
            return entity | (blockEntity << 16) | (item << 32);
        } catch (Throwable e) {
            YesSteveModel.LOGGER.error("Failed to read Iris entity id", e);
            return 0L;
        }
    }

    /**
     * 解析顶点格式基准。拿不到返回 null
     */
    private static VertexFormat resolveEntityFormat() throws Throwable {
        var formatsClass = resolveClass("vertices.IrisVertexFormats");
        if (formatsClass == null) {
            return null;
        }
        return (VertexFormat) formatsClass.getField("ENTITY").get(null);
    }

    /**
     * 解析 CapturedRenderingState.INSTANCE 及其三个取值方法
     */
    private static boolean resolveCapturedRenderingState(MethodHandles.Lookup lookup) {
        try {
            var stateClass = resolveClass("uniforms.CapturedRenderingState");
            if (stateClass == null) {
                return false;
            }
            var instance = stateClass.getField("INSTANCE").get(null);
            renderedEntityGetter = lookup.findVirtual(stateClass, "getCurrentRenderedEntity",
                    MethodType.methodType(int.class)).bindTo(instance);
            renderedBlockEntityGetter = lookup.findVirtual(stateClass, "getCurrentRenderedBlockEntity",
                    MethodType.methodType(int.class)).bindTo(instance);
            renderedItemGetter = lookup.findVirtual(stateClass, "getCurrentRenderedItem",
                    MethodType.methodType(int.class)).bindTo(instance);
            return true;
        } catch (Throwable e) {
            YesSteveModel.LOGGER.warn("Failed to resolve Iris CapturedRenderingState", e);
            return false;
        }
    }

    /**
     * 依次尝试新版与旧版包名，都找不到返回 null（Iris 未安装或版本不匹配）
     */
    private static Class<?> resolveClass(String suffix) {
        for (var prefix : new String[]{MODERN_PACKAGE, LEGACY_PACKAGE}) {
            try {
                return Class.forName(prefix + suffix);
            } catch (Throwable ignored) {
                // 换下一个包名继续试
            }
        }
        return null;
    }

    private static void reportUnknownSize(VertexFormat vertexFormat) {
        if (reportedUnknownSize) {
            return;
        }
        reportedUnknownSize = true;
        YesSteveModel.LOGGER.error("Unsupported Iris ENTITY vertex size {} (native supports 54/55/56); "
                        + "models fall back to the fallback layout under shaders. Elements: {}",
                vertexFormat.getVertexSize(), vertexFormat.getElementAttributeNames());
    }
}
