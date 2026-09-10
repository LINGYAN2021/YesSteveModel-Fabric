package com.elfmcys.ysm.client.compat.slashblade;

/**
 * 兼容层桩：联动模组在 Fabric 26.1.2 上不存在，全部返回安全默认值。
 * 保留原公开签名以便后续重新对接。
 */
public class SlashBladeCompat {
    public static void init() {
    }

    public static boolean isSlashBladeLoaded() {
        return false;
    }

    public static boolean isSlashBladeItem(net.minecraft.world.item.ItemStack stack) {
        return false;
    }

    public static String getAnimationName(
            com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent<? extends com.elfmcys.ysm.client.entity.CustomHumanoidEntity<?>> event) {
        return null;
    }

    public static com.elfmcys.ysm.geckolib3.core.PlayState playMainAnimation(
            net.minecraft.world.entity.LivingEntity livingEntity,
            com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent<? extends com.elfmcys.ysm.client.entity.CustomHumanoidEntity<?>> event,
            String animationName, com.elfmcys.ysm.geckolib3.core.builder.LoopType loopType) {
        return null;
    }

    public static void addBinding(com.elfmcys.ysm.client.animation.molang.CtrlBinding binding) {
    }

    public static boolean isResharped() {
        return false;
    }
}
