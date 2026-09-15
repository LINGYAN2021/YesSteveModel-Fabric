package com.elfmcys.ysm.client.compat.curios;

import com.elfmcys.ysm.geckolib3.core.molang.binding.ContextBinding;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Curios 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class CuriosCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static boolean hasAnyItemEquipped(LivingEntity entity, String slotType, ReferenceOpenHashSet<Item> items) {
        return false;
    }

    public static boolean hasAnyItemEquippedWithAnyTag(LivingEntity entity, String slotType, List<TagKey<Item>> tags) {
        return false;
    }

    public static boolean hasAnyItemEquippedWithAllTag(LivingEntity entity, String slotType, List<TagKey<Item>> tags) {
        return false;
    }

    public static void addMolangBinding(ContextBinding binding) {
    }
}
