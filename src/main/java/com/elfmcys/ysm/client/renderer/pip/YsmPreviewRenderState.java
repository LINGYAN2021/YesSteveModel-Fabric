package com.elfmcys.ysm.client.renderer.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * YSM GUI 预览的 Picture-in-Picture 渲染状态。
 * <p>
 * 26.1.2 的 GUI 渲染改为 extract/submit 两段式，屏幕在 extract 阶段把
 * 要渲染的内容打包成此对象提交，真正的渲染延迟到
 * {@link YsmPreviewRenderer#renderToTexture} 中执行。
 * <p>
 * <b>重要</b>：原版 {@code PictureInPictureRenderer} 每个渲染器实例只持有一张离屏纹理，
 * 且 blit 延迟到整帧末尾统一绘制——同一帧里同一个状态类出现两个状态就会共用纹理，
 * 最终所有 blit 采样到最后一次渲染的内容（表现为"所有格子显示同一个模型"）。
 * 因此这里按用途分子类，每种用途各自注册一个渲染器实例，互不干扰。
 */
public class YsmPreviewRenderState implements PictureInPictureRenderState {

    private final List<VanillaEntityEntry> vanillaEntities;
    @Nullable
    private final YsmRenderTask ysmEntity;
    private final List<BlockEntry> blocks;
    private final List<BlockEntityEntry> blockEntities;
    private final Matrix4f poseMatrix;
    private final int x0;
    private final int y0;
    private final int x1;
    private final int y1;
    private final float scale;
    @Nullable
    private final ScreenRectangle scissorArea;
    private final ScreenRectangle bounds;
    private final boolean paperDoll;

    public YsmPreviewRenderState(List<VanillaEntityEntry> vanillaEntities, @Nullable YsmRenderTask ysmEntity,
                                 List<BlockEntry> blocks, List<BlockEntityEntry> blockEntities,
                                 Matrix4f poseMatrix, int x0, int y0, int x1, int y1, float scale,
                                 @Nullable ScreenRectangle scissorArea, ScreenRectangle bounds,
                                 boolean paperDoll) {
        this.vanillaEntities = vanillaEntities;
        this.ysmEntity = ysmEntity;
        this.blocks = blocks;
        this.blockEntities = blockEntities;
        this.poseMatrix = poseMatrix;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.scale = scale;
        this.scissorArea = scissorArea;
        this.bounds = bounds;
        this.paperDoll = paperDoll;
    }

    public YsmPreviewRenderState(List<VanillaEntityEntry> vanillaEntities, @Nullable YsmRenderTask ysmEntity,
                                 List<BlockEntry> blocks, List<BlockEntityEntry> blockEntities,
                                 Matrix4f poseMatrix, ScreenRectangle area, float scale,
                                 @Nullable ScreenRectangle scissorArea, boolean paperDoll) {
        this(vanillaEntities, ysmEntity, blocks, blockEntities, poseMatrix,
                area.left(), area.top(), area.right(), area.bottom(), scale, scissorArea,
                PictureInPictureRenderState.getBounds(area.left(), area.top(), area.right(), area.bottom(), scissorArea),
                paperDoll);
    }

    public List<VanillaEntityEntry> vanillaEntities() {
        return vanillaEntities;
    }

    @Nullable
    public YsmRenderTask ysmEntity() {
        return ysmEntity;
    }

    public List<BlockEntry> blocks() {
        return blocks;
    }

    public List<BlockEntityEntry> blockEntities() {
        return blockEntities;
    }

    public Matrix4f poseMatrix() {
        return poseMatrix;
    }

    @Override
    public int x0() {
        return x0;
    }

    @Override
    public int y0() {
        return y0;
    }

    @Override
    public int x1() {
        return x1;
    }

    @Override
    public int y1() {
        return y1;
    }

    @Override
    public float scale() {
        return scale;
    }

    @Nullable
    @Override
    public ScreenRectangle scissorArea() {
        return scissorArea;
    }

    @Override
    public ScreenRectangle bounds() {
        return bounds;
    }

    public boolean paperDoll() {
        return paperDoll;
    }

    private static final class PaperDollImpl extends YsmPreviewRenderState {
        PaperDollImpl(List<VanillaEntityEntry> vanillaEntities, @Nullable YsmRenderTask ysmEntity,
                      List<BlockEntry> blocks, List<BlockEntityEntry> blockEntities,
                      Matrix4f poseMatrix, ScreenRectangle area, float scale,
                      @Nullable ScreenRectangle scissorArea, boolean paperDoll) {
            super(vanillaEntities, ysmEntity, blocks, blockEntities, poseMatrix, area, scale, scissorArea, paperDoll);
        }
    }

    private static final class ModelGridImpl extends YsmPreviewRenderState {
        ModelGridImpl(List<VanillaEntityEntry> vanillaEntities, @Nullable YsmRenderTask ysmEntity,
                      List<BlockEntry> blocks, List<BlockEntityEntry> blockEntities,
                      Matrix4f poseMatrix, ScreenRectangle area, float scale,
                      @Nullable ScreenRectangle scissorArea, boolean paperDoll) {
            super(vanillaEntities, ysmEntity, blocks, blockEntities, poseMatrix, area, scale, scissorArea, paperDoll);
        }
    }

    private static final class TextureGridImpl extends YsmPreviewRenderState {
        TextureGridImpl(List<VanillaEntityEntry> vanillaEntities, @Nullable YsmRenderTask ysmEntity,
                        List<BlockEntry> blocks, List<BlockEntityEntry> blockEntities,
                        Matrix4f poseMatrix, ScreenRectangle area, float scale,
                        @Nullable ScreenRectangle scissorArea, boolean paperDoll) {
            super(vanillaEntities, ysmEntity, blocks, blockEntities, poseMatrix, area, scale, scissorArea, paperDoll);
        }
    }

    private static final class ModelCard0Impl extends YsmPreviewRenderState {
        ModelCard0Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float sc,
                         @Nullable ScreenRectangle sr, boolean d) { super(v, t, b, e, p, a, sc, sr, d); }
    }
    private static final class ModelCard1Impl extends YsmPreviewRenderState {
        ModelCard1Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float sc,
                         @Nullable ScreenRectangle sr, boolean d) { super(v, t, b, e, p, a, sc, sr, d); }
    }
    private static final class ModelCard2Impl extends YsmPreviewRenderState {
        ModelCard2Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float sc,
                         @Nullable ScreenRectangle sr, boolean d) { super(v, t, b, e, p, a, sc, sr, d); }
    }
    private static final class ModelCard3Impl extends YsmPreviewRenderState {
        ModelCard3Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float sc,
                         @Nullable ScreenRectangle sr, boolean d) { super(v, t, b, e, p, a, sc, sr, d); }
    }
    private static final class ModelCard4Impl extends YsmPreviewRenderState {
        ModelCard4Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float sc,
                         @Nullable ScreenRectangle sr, boolean d) { super(v, t, b, e, p, a, sc, sr, d); }
    }
    private static final class ModelCard5Impl extends YsmPreviewRenderState {
        ModelCard5Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float sc,
                         @Nullable ScreenRectangle sr, boolean d) { super(v, t, b, e, p, a, sc, sr, d); }
    }
    private static final class ModelCard6Impl extends YsmPreviewRenderState {
        ModelCard6Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float sc,
                         @Nullable ScreenRectangle sr, boolean d) { super(v, t, b, e, p, a, sc, sr, d); }
    }
    private static final class ModelCard7Impl extends YsmPreviewRenderState {
        ModelCard7Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float sc,
                         @Nullable ScreenRectangle sr, boolean d) { super(v, t, b, e, p, a, sc, sr, d); }
    }
    private static final class ModelCard8Impl extends YsmPreviewRenderState {
        ModelCard8Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float sc,
                         @Nullable ScreenRectangle sr, boolean d) { super(v, t, b, e, p, a, sc, sr, d); }
    }
    private static final class ModelCard9Impl extends YsmPreviewRenderState {
        ModelCard9Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float sc,
                         @Nullable ScreenRectangle sr, boolean d) { super(v, t, b, e, p, a, sc, sr, d); }
    }

    private static final class TextureCard0Impl extends YsmPreviewRenderState {
        TextureCard0Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float s,
                         @Nullable ScreenRectangle sc, boolean d) { super(v, t, b, e, p, a, s, sc, d); }
    }

    private static final class TextureCard1Impl extends YsmPreviewRenderState {
        TextureCard1Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float s,
                         @Nullable ScreenRectangle sc, boolean d) { super(v, t, b, e, p, a, s, sc, d); }
    }

    private static final class TextureCard2Impl extends YsmPreviewRenderState {
        TextureCard2Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float s,
                         @Nullable ScreenRectangle sc, boolean d) { super(v, t, b, e, p, a, s, sc, d); }
    }

    private static final class TextureCard3Impl extends YsmPreviewRenderState {
        TextureCard3Impl(List<VanillaEntityEntry> v, @Nullable YsmRenderTask t, List<BlockEntry> b,
                         List<BlockEntityEntry> e, Matrix4f p, ScreenRectangle a, float s,
                         @Nullable ScreenRectangle sc, boolean d) { super(v, t, b, e, p, a, s, sc, d); }
    }

    private static final class TexturePreviewImpl extends YsmPreviewRenderState {
        TexturePreviewImpl(List<VanillaEntityEntry> vanillaEntities, @Nullable YsmRenderTask ysmEntity,
                           List<BlockEntry> blocks, List<BlockEntityEntry> blockEntities,
                           Matrix4f poseMatrix, ScreenRectangle area, float scale,
                           @Nullable ScreenRectangle scissorArea, boolean paperDoll) {
            super(vanillaEntities, ysmEntity, blocks, blockEntities, poseMatrix, area, scale, scissorArea, paperDoll);
        }
    }

    /**
     * 按用途创建状态：每种用途对应独立的渲染器实例与离屏纹理。
     */
    public static YsmPreviewRenderState of(Lane lane, List<VanillaEntityEntry> vanillaEntities,
                                           @Nullable YsmRenderTask ysmEntity, List<BlockEntry> blocks,
                                           List<BlockEntityEntry> blockEntities, Matrix4f poseMatrix,
                                           ScreenRectangle area, float scale,
                                           @Nullable ScreenRectangle scissorArea, boolean paperDoll) {
        return switch (lane) {
            case PAPER_DOLL -> new PaperDollImpl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_GRID -> new ModelGridImpl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case TEXTURE_GRID -> new TextureGridImpl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case TEXTURE_PREVIEW -> new TexturePreviewImpl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case TEXTURE_CARD_0 -> new TextureCard0Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case TEXTURE_CARD_1 -> new TextureCard1Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case TEXTURE_CARD_2 -> new TextureCard2Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case TEXTURE_CARD_3 -> new TextureCard3Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_CARD_0 -> new ModelCard0Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_CARD_1 -> new ModelCard1Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_CARD_2 -> new ModelCard2Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_CARD_3 -> new ModelCard3Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_CARD_4 -> new ModelCard4Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_CARD_5 -> new ModelCard5Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_CARD_6 -> new ModelCard6Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_CARD_7 -> new ModelCard7Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_CARD_8 -> new ModelCard8Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
            case MODEL_CARD_9 -> new ModelCard9Impl(vanillaEntities, ysmEntity, blocks, blockEntities,
                    poseMatrix, area, scale, scissorArea, paperDoll);
        };
    }

    /**
     * 预览用途分道
     */
    public enum Lane {
        PAPER_DOLL,
        MODEL_GRID,
        TEXTURE_GRID,
        TEXTURE_PREVIEW,
        /** 模型选择界面每格一条道：同帧多个同尺寸预览不能共用纹理，否则全部显示最后一个 */
        MODEL_CARD_0,
        MODEL_CARD_1,
        MODEL_CARD_2,
        MODEL_CARD_3,
        MODEL_CARD_4,
        MODEL_CARD_5,
        MODEL_CARD_6,
        MODEL_CARD_7,
        MODEL_CARD_8,
        MODEL_CARD_9,
        /** 贴图页每张卡片各自一条道 */
        TEXTURE_CARD_0,
        TEXTURE_CARD_1,
        TEXTURE_CARD_2,
        TEXTURE_CARD_3;

        public static Lane modelCard(int slot) {
            return switch (Math.floorMod(slot, 10)) {
                case 0 -> MODEL_CARD_0;
                case 1 -> MODEL_CARD_1;
                case 2 -> MODEL_CARD_2;
                case 3 -> MODEL_CARD_3;
                case 4 -> MODEL_CARD_4;
                case 5 -> MODEL_CARD_5;
                case 6 -> MODEL_CARD_6;
                case 7 -> MODEL_CARD_7;
                case 8 -> MODEL_CARD_8;
                default -> MODEL_CARD_9;
            };
        }

        public static Lane textureCard(int slot) {
            return switch (Math.floorMod(slot, 4)) {
                case 0 -> TEXTURE_CARD_0;
                case 1 -> TEXTURE_CARD_1;
                case 2 -> TEXTURE_CARD_2;
                default -> TEXTURE_CARD_3;
            };
        }
    }

    /** 各分道对应的具体状态类（注册 PiP 渲染器用） */
    public static List<Class<? extends PictureInPictureRenderState>> laneTypes() {
        return List.of(PaperDollImpl.class, ModelGridImpl.class, TextureGridImpl.class,
                TexturePreviewImpl.class, TextureCard0Impl.class, TextureCard1Impl.class,
                TextureCard2Impl.class, TextureCard3Impl.class, ModelCard0Impl.class, ModelCard1Impl.class, ModelCard2Impl.class, ModelCard3Impl.class, ModelCard4Impl.class, ModelCard5Impl.class, ModelCard6Impl.class, ModelCard7Impl.class, ModelCard8Impl.class, ModelCard9Impl.class);
    }

    /**
     * 走原版 EntityRenderDispatcher 的附加实体（马/猪/船等坐骑预览）。
     * 状态的 x/y/z 等字段在 extract 阶段已被覆写为 GUI 内的局部坐标。
     */
    public record VanillaEntityEntry(EntityRenderState state) {
    }

    public record BlockEntry(BlockState state, Vector3f offset) {
    }

    public record BlockEntityEntry(BlockEntityRenderState state, Vector3f offset) {
    }

    /**
     * YSM 自定义模型预览的渲染任务。execute 内部需要自行完成
     * 实体字段的保存、覆写与恢复。
     */
    @FunctionalInterface
    public interface YsmRenderTask {
        void execute(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState);
    }
}
