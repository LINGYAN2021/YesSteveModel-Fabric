package com.elfmcys.ysm.client.compat.touhoulittlemaid.client;

/**
 * 兼容层桩：联动模组在 Fabric 26.1.2 上不存在，全部返回安全默认值。
 * 保留原公开签名以便后续重新对接。
 */
public class TlmClientCompat {
    public static void init() {
    }

    public static Object buildControllerFactory(com.elfmcys.ysm.client.model.PlayerModelResources model,
            com.elfmcys.ysm.client.model.CommonAsset assets) {
        return null;
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean isMaid(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    public static boolean hasMaidCap(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    public static boolean isChair(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    public static boolean isSit(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    public static boolean isGohei(net.minecraft.world.item.Item item) {
        return false;
    }

    public static String getChairId(net.minecraft.world.entity.Entity entity) {
        return null;
    }

    public static boolean isMaidFishing(net.minecraft.world.entity.LivingEntity entity) {
        return false;
    }

    public static void addBinding(com.elfmcys.ysm.client.animation.molang.TLMBinding binding) {
    }

    public static com.elfmcys.ysm.geckolib3.core.PlayState getMaidVehicleAnimation(
            com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent<com.elfmcys.ysm.client.entity.CustomHumanoidEntity<?>> event,
            net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.entity.Entity vehicle) {
        return null;
    }

    public static void markTacGunAnimationNeedReload(net.minecraft.world.entity.LivingEntity entity) {
    }

    public static boolean pointToMaid() {
        return false;
    }

    public static void onRouletteMainKeyPressed() {
    }
}
