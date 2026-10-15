package com.elfmcys.ysm.client.renderer;

import com.elfmcys.ysm.capability.PlayerAnimatableCapability;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.entity.CustomFirstPersonArmEntity;
import com.elfmcys.ysm.geckolib3.geo.GeoEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.HumanoidArm;

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
     * 相机位于眼位（右手系：+X 右、+Y 上、-Z 前方）。手臂模型与主模型同属
     * "脚底为原点、16 单位/格"的空间，肩部枢轴在模型内 y≈1.86 格、x≈±0.072 格
     * （LeftArm/RightArm pivot = ±1.15, 29.775, 0）。
     *
     * <p>锚点直接取原版第一人称手臂的位置：原版 renderPlayerArm 里做的是
     * {@code translate(side*0.64, -0.6, -0.72)} + 绕 Y {@code side*45°}。
     * 把模型的肩部对齐到该锚点，反推出模型原点应放的位置，所以这里的
     * X/Y 是"锚点减肩部枢轴"（0.64+0.05、-0.6-1.86、-0.72-0.05）。
     */
    private static final float ARM_X = 0.69F;
    private static final float ARM_Y = -2.46F;
    private static final float ARM_Z = -0.77F;

    /**
     * 模型建模时正面朝 +Z（与相机同向的是 -Z），不翻的话手臂会正对镜头伸过来
     * （只能看到袖口端面）
     */
    private static final float ARM_FLIP_YAW = 180.0F;

    /**
     * 与原版一致的 45° 内旋，让手臂从外侧朝屏幕中央斜伸
     */
    private static final float ARM_INWARD_YAW = 45.0F;

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
            // 按主手镜像：主手为右时锚点在右侧、内旋 +45°，主手为左时全部取反
            float side = player.getMainArm() == HumanoidArm.RIGHT ? 1.0F : -1.0F;
            poseStack.translate(side * ARM_X, ARM_Y, ARM_Z);
            poseStack.mulPose(Axis.YP.rotationDegrees(ARM_FLIP_YAW + side * ARM_INWARD_YAW));
            drawnThisFrame = false;
            render(armEntity, 180.0F, partialTick, poseStack, collector, packedLight,
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
