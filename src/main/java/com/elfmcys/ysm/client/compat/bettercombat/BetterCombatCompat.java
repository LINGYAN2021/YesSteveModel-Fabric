package com.elfmcys.ysm.client.compat.bettercombat;

/**
 * 兼容层桩：联动模组在 Fabric 26.1.2 上不存在，全部返回安全默认值。
 * 保留原公开签名以便后续重新对接。
 */
public class BetterCombatCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static void addBinding(com.elfmcys.ysm.client.animation.molang.CtrlBinding binding) {
    }
}
