package com.elfmcys.ysm.client.compat.tacz;

/**
 * 兼容层桩：联动模组在 Fabric 26.1.2 上不存在，全部返回安全默认值。
 * 保留原公开签名以便后续重新对接。
 */
public class TACZCompat {
    public static void init() {
    }

    public static void addBinding(com.elfmcys.ysm.client.animation.molang.CtrlBinding ctrlBinding) {
    }

    public static void renderOffsetHand(net.minecraft.world.item.ItemStack offhandItem,
            com.elfmcys.ysm.geckolib3.model.AnimatedGeoModel geoModel,
            net.minecraft.world.entity.LivingEntity livingEntity,
            com.mojang.blaze3d.vertex.PoseStack poseStack, int packedLight, float partialTicks) {
    }

    public static com.elfmcys.ysm.geckolib3.core.PlayState playGunMainAnimation(
            net.minecraft.world.entity.LivingEntity livingEntity,
            com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent<? extends com.elfmcys.ysm.client.entity.CustomHumanoidEntity<?>> event,
            String animationName, com.elfmcys.ysm.geckolib3.core.builder.LoopType loopType) {
        return null;
    }

    public static com.elfmcys.ysm.geckolib3.core.PlayState playGunHoldAnimation(
            net.minecraft.world.item.ItemStack mainHandItem,
            com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent<? extends com.elfmcys.ysm.client.entity.CustomHumanoidEntity<?>> event) {
        return null;
    }

    public static com.elfmcys.ysm.geckolib3.core.PlayState playGunOnceAnimation(
            net.minecraft.world.item.ItemStack mainHandItem,
            com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent<? extends com.elfmcys.ysm.client.entity.CustomHumanoidEntity<?>> event) {
        return null;
    }

    public static void openFlashShellRender(net.minecraft.world.entity.LivingEntity livingEntity,
            net.minecraft.world.item.ItemStack mainHandItem) {
    }

    public static void stopFlashShellRender(net.minecraft.world.item.ItemStack mainHandItem) {
    }

    public static net.minecraft.resources.Identifier getGunId(net.minecraft.world.item.ItemStack stack) {
        return null;
    }

    public static boolean isInstalled() {
        return false;
    }
}
