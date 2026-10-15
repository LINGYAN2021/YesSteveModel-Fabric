package com.elfmcys.ysm.client.renderer;

import com.elfmcys.ysm.capability.PlayerAnimatableCapability;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.entity.CustomFirstPersonArmEntity;
import com.elfmcys.ysm.geckolib3.geo.GeoEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * 第一人称手臂渲染器。
 *
 * <p>手臂模型（{@code models/arm.json}）本身包含左右两条手臂的骨骼
 * （LeftArm/LeftForeArm/LeftHandLocator 与 RightArm/RightForeArm/RightHandLocator），
 * 所以每帧只渲染一次整体，而不是每只手渲染一次。
 * 由 {@code ItemInHandRendererMixin} 在 {@code renderHandsWithItems} 开头调用，
 * 成功后由同一 mixin 拦掉原版手臂（手持物品仍由原版负责绘制）。
 *
 * <p>拿不到模型或模型尚未加载时返回 false，调用方保留原版手臂——
 * 宁可显示原版手臂，也不要在第一人称里把手臂弄丢。
 *
 * <p>{@link #ARM_Y} 等常量是手臂在相机空间中的摆放，属于需要进游戏目视微调的数值。
 */
public class CustomFirstPersonArmRenderer extends GeoEntityRenderer<LocalPlayer, CustomFirstPersonArmEntity> {
    /**
     * 摆放完全照搬官方实现（从官方 2.6.5 的渲染类字节码里读出来的）：
     * 官方在相机空间的姿态栈上只做了一句 {@code translate(0.0, -1.5, 0.0)}，
     * 没有任何旋转、没有 X/Z 偏移，然后用专用手臂渲染模式绘制模型。
     *
     * <p>所以这里也保持同样的极简做法。模型朝向由渲染器的 (180 - yaw) 负责，
     * 传 180 时该旋转为 0，模型保持建模朝向，与官方一致。
     */
    private static final float ARM_X = 0.0F;
    private static final float ARM_Y = -1.5F;
    private static final float ARM_Z = 0.0F;

    /**
     * 传给渲染器的 yaw。渲染器会做 (180 - yaw)，所以这里必须传 0，
     * 让它翻转 180°：手臂模型的动画姿态是朝 +Z 伸出的（相机看向 -Z），
     * 不翻的话手臂会伸到相机身后、直接被近裁剪面裁掉，表现为"完全没有手"。
     */
    private static final float ARM_YAW = 0.0F;

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

        poseStack.pushPose();
        try {
            poseStack.translate(ARM_X, ARM_Y, ARM_Z);
            drawnThisFrame = false;
            render(armEntity, ARM_YAW, partialTick, poseStack, collector, packedLight,
                    new CameraRenderState());
        } finally {
            poseStack.popPose();
        }

        hideVanillaArm = drawnThisFrame;
        return hideVanillaArm;
    }

    /**
     * 记录本帧是否真的画出了东西。只有确认画出来了才允许隐藏原版手臂，
     * 否则在模型状态无效或贴图缺失时会出现"两只手都没有"。
     */
    @Override
    public void render(CustomFirstPersonArmEntity animatable, float yaw, float partialTick,
                       PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       CameraRenderState cameraState) {
        var data = animatable.update(partialTick);
        if (data == null || !data.modelState.isValid()) {
            diag("skip: render data null or model state invalid");
            return;
        }
        super.render(animatable, yaw, partialTick, poseStack, collector, packedLight, cameraState);
        drawnThisFrame = true;
        diag("drew custom arm");
    }

    public boolean shouldHideVanillaArm() {
        return hideVanillaArm;
    }

    /**
     * 手臂是本地玩家自己的模型，不需要画名字
     */
    @Override
    protected void renderNameTag(LocalPlayer entity, PoseStack poseStack, SubmitNodeCollector collector,
                                 CameraRenderState cameraState, int packedLight) {
    }
}
