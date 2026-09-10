package com.elfmcys.ysm.client.compat.parcool;

/**
 * 兼容层桩：联动模组在 Fabric 26.1.2 上不存在，全部返回安全默认值。
 * 保留原公开签名以便后续重新对接。
 */
public class ParCoolCompat {
    public static void init() {
    }

    public static java.util.Optional<org.apache.commons.lang3.tuple.Pair<String, String>> getCompatibilityWarning() {
        return java.util.Optional.empty();
    }

    public static boolean isInstalled() {
        return false;
    }

    public static java.util.Optional<java.util.function.BiFunction<String,
            com.elfmcys.ysm.client.entity.CustomPlayerEntity,
            com.elfmcys.ysm.geckolib3.core.controller.IAnimationController<com.elfmcys.ysm.client.entity.CustomPlayerEntity>>> animationPredicate() {
        return java.util.Optional.empty();
    }

    public static boolean hasAnimation(net.minecraft.world.entity.player.Player player) {
        return false;
    }

    public static String getAnimation(net.minecraft.world.entity.player.Player player) {
        return null;
    }

    public static void addBinding(com.elfmcys.ysm.client.animation.molang.CtrlBinding binding) {
    }
}
