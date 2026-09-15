package com.elfmcys.ysm.client.compat.create;

import com.elfmcys.ysm.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.player.Player;

/**
 * Create 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class CreateCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean isHangingSkyhook(Player player) {
        return false;
    }

    public static void addBinding(CtrlBinding binding) {
    }
}
