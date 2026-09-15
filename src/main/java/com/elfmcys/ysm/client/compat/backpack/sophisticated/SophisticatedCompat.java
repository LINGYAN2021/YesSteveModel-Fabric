package com.elfmcys.ysm.client.compat.backpack.sophisticated;

import com.elfmcys.ysm.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

/**
 * Sophisticated Backpacks 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class SophisticatedCompat {
    public static void init() {
    }

    public static Optional<Pair<String, String>> getCompatibilityWarning() {
        return Optional.empty();
    }

    public static void addLayer() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static void addBinding(CtrlBinding binding) {
    }

    public static ItemStack getBackpackItemStack(LivingEntity livingEntity) {
        return ItemStack.EMPTY;
    }
}
