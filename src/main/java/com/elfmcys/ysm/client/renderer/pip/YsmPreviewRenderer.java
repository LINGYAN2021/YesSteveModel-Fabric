package com.elfmcys.ysm.client.renderer.pip;

import com.elfmcys.ysm.mixin.client.EntityRenderDispatcherAccessor;
import com.elfmcys.ysm.util.RenderUtil;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * YSM GUI 预览的 PiP 渲染器，负责把 {@link YsmPreviewRenderState}
 * 渲染到离屏纹理。渲染顺序与官方 2.6.5 保持一致：
 * 地面方块 → 床（方块实体）→ 原版坐骑实体 → YSM 自定义模型。
 */
public class YsmPreviewRenderer extends PictureInPictureRenderer<YsmPreviewRenderState> {
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final BlockModelRenderState blockModelRenderState = new BlockModelRenderState();

    public YsmPreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    protected void renderToTexture(YsmPreviewRenderState state, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        FeatureRenderDispatcher featureDispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeCollector collector = featureDispatcher.getSubmitNodeStorage();
        BlockModelResolver blockModelResolver = ((EntityRenderDispatcherAccessor) mc.getEntityRenderDispatcher()).ysm$blockModelResolver();

        RenderUtil.setRenderingInInventory(!state.paperDoll());
        RenderUtil.setRenderingInPaperDoll(state.paperDoll());
        try {
            poseStack.pushPose();
            try {
                poseStack.last().mulPose(state.poseMatrix());

                for (YsmPreviewRenderState.BlockEntry block : state.blocks()) {
                    poseStack.pushPose();
                    poseStack.translate(block.offset().x, block.offset().y, block.offset().z);
                    this.blockModelRenderState.clear();
                    blockModelResolver.update(this.blockModelRenderState, block.state(), BLOCK_DISPLAY_CONTEXT);
                    this.blockModelRenderState.submit(poseStack, collector, -1, OverlayTexture.NO_OVERLAY, -1);
                    poseStack.popPose();
                }

                CameraRenderState cameraState = new CameraRenderState();
                for (YsmPreviewRenderState.BlockEntityEntry blockEntity : state.blockEntities()) {
                    poseStack.pushPose();
                    poseStack.translate(blockEntity.offset().x, blockEntity.offset().y, blockEntity.offset().z);
                    poseStack.translate(-blockEntity.state().blockPos.getX(),
                            -blockEntity.state().blockPos.getY(),
                            -blockEntity.state().blockPos.getZ());
                    mc.getBlockEntityRenderDispatcher().submit(blockEntity.state(), poseStack, collector, cameraState);
                    poseStack.popPose();
                }

                for (YsmPreviewRenderState.VanillaEntityEntry entity : state.vanillaEntities()) {
                    EntityRenderState entityState = entity.state();
                    mc.getEntityRenderDispatcher().submit(entityState, cameraState,
                            entityState.x, entityState.y, entityState.z, poseStack, collector);
                }

                if (state.ysmEntity() != null) {
                    state.ysmEntity().execute(poseStack, collector, cameraState);
                }
            } finally {
                poseStack.popPose();
            }
            // 实体的原生渲染发生在 renderAllFeatures 里，上下文标记必须覆盖到这里
            featureDispatcher.renderAllFeatures();
            this.bufferSource.endBatch();
        } finally {
            RenderUtil.setRenderingInInventory(false);
            RenderUtil.setRenderingInPaperDoll(false);
        }
    }

    @Override
    public Class<YsmPreviewRenderState> getRenderStateClass() {
        return YsmPreviewRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "ysm_preview";
    }
}
