package com.elfmcys.ysm.client.compat;

/**
 * 兼容层桩：联动模组在 Fabric 26.1.2 上不存在，全部返回安全默认值。
 */
public class IrisCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean isRenderingShadow() {
        return false;
    }

    public static long getEntityId() {
        return 0;
    }

    public static java.util.Optional<com.elfmcys.ysm.natives.render.VertexFormatType> determineVertexFormatType(
            com.mojang.blaze3d.vertex.VertexFormat vertexFormat) {
        return java.util.Optional.empty();
    }
}
