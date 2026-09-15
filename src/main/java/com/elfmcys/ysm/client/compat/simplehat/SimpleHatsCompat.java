package com.elfmcys.ysm.client.compat.simplehat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Simple Hats 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class SimpleHatsCompat {
    public static void init() {
    }

    public static ItemStack getCuriosHead(LivingEntity livingEntity) {
        return ItemStack.EMPTY;
    }
}
