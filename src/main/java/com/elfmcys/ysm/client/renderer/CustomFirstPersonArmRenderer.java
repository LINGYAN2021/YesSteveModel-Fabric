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
 * <p>摆放照搬官方 1.21.1 实现（官方 2.6.x fabric 渲染类字节码）：
 * 相机空间姿态栈上 {@code translate(0, -1.5, 0)}，不旋转、不缩放，并且
 * <b>不</b>乘模型的 widthScale/heightScale——官方第一人称直调
 * {@link NativeRenderer#render}，不像第三人称那样过 {@code preRender} 的缩放。
 * （官方 26.1 neoforge 为每只手 {@code translate(±0.25, 1.8, 0)} + {@code scale(-1,-1,1)}，
 * 语义等价于把模型翻转倒挂；两种摆法可用 {@code /ysmclient armpose} 切换微调。）
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

        // 与官方 1.21.1 一致：translate(0, -1.5, 0)，不旋转、不乘模型缩放
        poseStack.pushPose();
        try {
            poseStack.translate(ArmPoseState.x(), ArmPoseState.y(), ArmPoseState.z());
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
