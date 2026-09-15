package com.elfmcys.ysm.client.compat;

import net.minecraft.client.player.AbstractClientPlayer;

/**
 * 兼容模组在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class PlayerAnimatorCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean hasThirdPersonModelAnim(AbstractClientPlayer player) {
        return false;
    }
}
