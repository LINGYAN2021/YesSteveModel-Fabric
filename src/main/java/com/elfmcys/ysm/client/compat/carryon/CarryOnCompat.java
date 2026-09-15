package com.elfmcys.ysm.client.compat.carryon;

import com.elfmcys.ysm.client.animation.molang.CtrlBinding;
import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.core.controller.IAnimationController;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Carry On 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class CarryOnCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> animationPredicate() {
        return Optional.empty();
    }

    public static boolean isCarryOnPrincess(Player player) {
        return false;
    }

    public static void addBinding(CtrlBinding binding) {
    }
}
