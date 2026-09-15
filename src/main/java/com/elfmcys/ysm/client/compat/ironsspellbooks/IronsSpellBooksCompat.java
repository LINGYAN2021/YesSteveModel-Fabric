package com.elfmcys.ysm.client.compat.ironsspellbooks;

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
