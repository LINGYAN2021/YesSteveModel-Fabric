package com.elfmcys.ysm.client.compat;

/**
 * 兼容层桩：联动模组在 Fabric 26.1.2 上不存在，全部返回安全默认值。
 */
public class CosmeticArmorCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static java.util.Optional<net.minecraft.world.item.ItemStack> getSkinArmorItem(
            net.minecraft.world.entity.player.Player player, net.minecraft.world.entity.EquipmentSlot slot) {
        return java.util.Optional.empty();
    }
}
