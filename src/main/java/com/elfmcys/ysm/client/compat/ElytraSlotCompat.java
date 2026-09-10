package com.elfmcys.ysm.client.compat;

/**
 * 兼容层桩：联动模组在 Fabric 26.1.2 上不存在，全部返回安全默认值。
 */
public class ElytraSlotCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static net.minecraft.world.item.ItemStack getEquippedElytraItem(net.minecraft.world.entity.LivingEntity entity) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
