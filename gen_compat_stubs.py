#!/usr/bin/env python3
"""Generate no-op compat stubs for mods that don't exist on Fabric 26.1.2."""
from pathlib import Path

ROOT = Path(r"C:\Users\CHEN\CascadeProjects\YSM26.1.2FA\ysm\src\main\java\com\elfmcys\ysm\client\compat")

STUBS = {
    "ARCompat.java": """package com.elfmcys.ysm.client.compat;

/**
 * 兼容模组在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class ARCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }
}
""",
    "OptifineCompat.java": """package com.elfmcys.ysm.client.compat;

/**
 * 兼容模组在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class OptifineCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }
}
""",
    "PlayerAnimatorCompat.java": """package com.elfmcys.ysm.client.compat;

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
""",
    "SimplePlaneCompat.java": """package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.client.entity.CustomVehicleEntity;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * 兼容模组在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class SimplePlaneCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<Vector3f> getRotation(AnimationEvent<CustomVehicleEntity> event) {
        return Optional.empty();
    }
}
""",
    "ImmersiveAircraftCompat.java": """package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.client.entity.CustomVehicleEntity;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * 兼容模组在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class ImmersiveAircraftCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<Vector3f> getRotation(AnimationEvent<CustomVehicleEntity> event) {
        return Optional.empty();
    }
}
""",
    "curios/CuriosCompat.java": """package com.elfmcys.ysm.client.compat.curios;

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
""",
    "realcamera/RealCameraCompat.java": """package com.elfmcys.ysm.client.compat.realcamera;

/**
 * RealCamera 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class RealCameraCompat {
    public static void init() {
    }

    public static boolean isActive() {
        return false;
    }
}
""",
    "carryon/CarryOnCompat.java": """package com.elfmcys.ysm.client.compat.carryon;

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
""",
    "create/CreateCompat.java": """package com.elfmcys.ysm.client.compat.create;

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
""",
    "backpack/sophisticated/SophisticatedCompat.java": """package com.elfmcys.ysm.client.compat.backpack.sophisticated;

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
""",
    "swem/SwemCompat.java": """package com.elfmcys.ysm.client.compat.swem;

import com.elfmcys.ysm.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;

/**
 * SWEM 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class SwemCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static String getAnimation(LivingEntity livingEntity) {
        return "";
    }

    public static void addBinding(CtrlBinding binding) {
    }
}
""",
    "ironsspellbooks/IronsSpellBooksCompat.java": """package com.elfmcys.ysm.client.compat.ironsspellbooks;

import com.elfmcys.ysm.client.animation.molang.CtrlBinding;
import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.geckolib3.core.PlayState;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.world.entity.LivingEntity;

/**
 * Iron's Spellbooks 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class IronsSpellBooksCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static void addBinding(CtrlBinding binding) {
    }

    public static PlayState playAnimation(AnimationEvent<CustomHumanoidEntity<?>> event, LivingEntity entity) {
        return PlayState.CONTINUE;
    }
}
""",
    "immersivemelodies/ImmersiveMelodiesCompat.java": """package com.elfmcys.ysm.client.compat.immersivemelodies;

import com.elfmcys.ysm.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;

/**
 * Immersive Melodies 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class ImmersiveMelodiesCompat {
    public static void init() {
    }

    public static void updateMelodyProgress(LivingEntity entity, ImmersiveMelodiesData imData) {
    }

    public static void addBinding(CtrlBinding binding) {
    }

    public static final class ImmersiveMelodiesData {
        public float pitch = 0f;
        public float volume = 0f;
        public float current = 0f;
        public long delta = 0L;
        public long time = 0L;
    }
}
""",
    "simplehat/SimpleHatsCompat.java": """package com.elfmcys.ysm.client.compat.simplehat;

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
""",
}

for rel, content in STUBS.items():
    p = ROOT / rel
    if p.exists():
        print("SKIP (exists):", rel)
        continue
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content, encoding="utf-8")
    print("wrote:", rel)
