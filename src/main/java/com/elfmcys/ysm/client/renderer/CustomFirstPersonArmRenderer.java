package com.elfmcys.ysm.client.renderer;

import com.elfmcys.ysm.capability.PlayerAnimatableCapability;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.entity.CustomFirstPersonArmEntity;
import com.elfmcys.ysm.geckolib3.core.util.Color;
import com.elfmcys.ysm.geckolib3.geo.CustomTranslucentRenderType;
import com.elfmcys.ysm.geckolib3.geo.GeoEntityRenderer;
import com.elfmcys.ysm.natives.render.NativeRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;

/**
 * 第一人称手臂渲染器。
 *
 * <p>手臂模型（{@code models/arm.json}）本身包含左右两条手臂的骨骼
 * （LeftArm/LeftForeArm/LeftHandLocator 与 RightArm/RightForeArm/RightHandLocator），
 * 每帧只渲染一次整体。
 * 由 {@code ItemInHandRendererMixin} 在 {@code renderHandsWithItems} 开头调用，
 * 成功后由同一 mixin 拦掉原版手臂（手持物品仍由原版负责绘制）。
 *
 * <p>拿不到模型或模型尚未加载时返回 false，调用方保留原版手臂——
 * 宁可显示原版手臂，也不要在第一人称里把手臂弄丢。
 *
 * <p>摆放参数见 {@link ArmPoseState}（默认值为进游戏目视调整的结果，接近官方观感），
 * 关键是<b>不</b>乘模型的 widthScale/heightScale——官方第一人称直调
 * {@link NativeRenderer#render}，不像第三人称那样过 {@code preRender} 的缩放。
 * （参考：官方 1.21.1 为 {@code translate(0, -1.5, 0)}；官方 26.1 neoforge 为每只手
 * {@code translate(±0.25, 1.8, 0)} + {@code scale(-1,-1,1)}。）
 */
public class CustomFirstPersonArmRenderer extends GeoEntityRenderer<LocalPlayer, CustomFirstPersonArmEntity> {
    private CustomFirstPersonArmEntity armEntity;

    /**
     * 本帧是否已接管手臂；renderPlayerArm 据此决定是否跳过原版手臂
     */
    private boolean hideVanillaArm;

    /**
     * 本帧是否真的画出了手臂模型
     */
    private boolean drawnThisFrame;

    /**
     * 临时诊断：每种结果只报一次，用来定位第一人称手臂卡在哪一步
     */
    private static final java.util.Set<String> LOGGED_OUTCOMES =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static void diag(String outcome) {
        if (LOGGED_OUTCOMES.add(outcome)) {
            com.elfmcys.ysm.YesSteveModel.LOGGER.info("[YSM-ARM] {}", outcome);
        }
    }

    public CustomFirstPersonArmRenderer() {
        super(YsmRendererContexts.get());
    }

    /**
     * 渲染自定义手臂。
     *
     * @return true 表示已接管，调用方应隐藏原版手臂
     */
    public boolean renderArm(LocalPlayer player, float partialTick, PoseStack poseStack,
                             SubmitNodeCollector collector, int packedLight) {
        hideVanillaArm = false;

        PlayerAnimatableCapability cap = EntityCapabilityHolder
                .get(player, PlayerAnimatableCapabilityProvider.CAP).orElse(null);
        if (cap == null || cap.getModelRenderTarget() == null || player.isInvisible()) {
            diag("skip: no capability / no render target / player invisible");
            return false;
        }

        if (armEntity == null || armEntity.getEntity() != player) {
            armEntity = new CustomFirstPersonArmEntity(player, cap);
        }
        armEntity.checkModelUpdate();
        if (armEntity.getLoadedGeoModel() == null || armEntity.getModelRenderTarget() == null) {
            // 模型还在加载：让原版手臂先顶着
            diag("skip: arm model not ready yet");
            return false;
        }

        var data = armEntity.update(partialTick);
        if (data == null || !data.modelState.isValid()) {
            diag("skip: render data null or model state invalid");
            return false;
        }

        var renderType = data.modelState.hasTranslucentVertices()
                ? CustomTranslucentRenderType.create(data.texture)
                : RenderTypes.armorCutoutNoCull(data.texture);

        // 照搬原版 renderPlayerArm 空手的变换链（不含挥动项），让自定义手臂落在
        // 原版手臂的位置——第一人称手臂的正确观感由这条链保证，而不是手摆参数。
        poseStack.pushPose();
        try {
            boolean mainIsRight = player.getMainArm() == HumanoidArm.RIGHT;
            float invert = mainIsRight ? 1.0F : -1.0F;
            poseStack.translate(invert * 0.64F, -0.6F, -0.72F);
            poseStack.mulPose(Axis.YP.rotationDegrees(invert * 45.0F));
            poseStack.translate(invert * -1.0F, 3.6F, 3.5F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(invert * 120.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(invert * -135.0F));
            poseStack.translate(invert * 5.6F, 0.0F, 0.0F);

            // 再把手臂模型对齐到原版手臂部件的原点：手臂模型肩 pivot 约在
            // (±0.072, 1.861, 0)（方块单位），手臂沿 -y 向下延伸；原版部件沿 +y
            // 延伸，所以 scale(1,-1,-1)（=绕 X 转 180°，行列式为正不翻面）对齐。
            // 左撇子主手时画的是 LeftArm，对齐偏移的 x 跟着镜像。
            // ArmPoseState 的 x/y/z 就是这段对齐偏移，可用 /ysmclient armpose 微调。
            float alignX = mainIsRight ? ArmPoseState.x() : -ArmPoseState.x();
            poseStack.translate(alignX, ArmPoseState.y(), ArmPoseState.z());
            if (ArmPoseState.pitch() != 0.0F) {
                poseStack.mulPose(Axis.XP.rotationDegrees(ArmPoseState.pitch()));
            }
            if (ArmPoseState.yaw() != 0.0F) {
                poseStack.mulPose(Axis.YP.rotationDegrees(ArmPoseState.yaw()));
            }
            poseStack.scale(ArmPoseState.sx(), ArmPoseState.sy(), ArmPoseState.sz());

            collector.submitCustomGeometry(poseStack, renderType,
                    (pose, consumer) -> NativeRenderer.render(
                            consumer, pose, data.modelState.getNativeState(),
                            data.modelState.getVertexCount(), packedLight,
                            OverlayTexture.NO_OVERLAY, Color.WHITE.getColor(), data.ctx.nativeType()));
        } finally {
            poseStack.popPose();
        }

        drawnThisFrame = true;
        hideVanillaArm = true;
        diag("drew custom arm");
        return true;
    }

    public boolean shouldHideVanillaArm() {
        return hideVanillaArm;
    }
}
