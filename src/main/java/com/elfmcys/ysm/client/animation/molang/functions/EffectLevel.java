package com.elfmcys.ysm.client.animation.molang.functions;

import com.elfmcys.ysm.capability.PlayerAnimatableCapability;
import com.elfmcys.ysm.geckolib3.core.molang.context.IContext;
import com.elfmcys.ysm.geckolib3.core.molang.function.ContextFunction;
import com.elfmcys.ysm.mixin.client.ArrowEntityAccessor;
import com.elfmcys.ysm.molang.runtime.ExecutionContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

public class EffectLevel extends ContextFunction<Entity> {
    @Override
    public boolean validateArgumentSize(int size) {
        return size >= 1;
    }

    @Override
    protected Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
        int sum = 0;
        for (var i = 0; i < arguments.size(); ++i) {
            Identifier effectId = arguments.getAsResourceLocation(context, i);
            if (effectId == null) {
                continue;
            }

            Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.get(effectId).orElse(null);
            if (effect == null) {
                continue;
            }

            if (context.entity().animatableEntity() instanceof PlayerAnimatableCapability cap && !cap.isLocalPlayer()) {
                sum += cap.getStateTracker().getEffectLevel(effect);
            } else if (context.entity().entity() instanceof LivingEntity) {
                MobEffectInstance instance = ((LivingEntity) context.entity().entity()).getEffect(effect);
                if (instance != null) {
                    sum += instance.getAmplifier() + 1;
                }
            } else if (context.entity().entity() instanceof Arrow) {
                for (MobEffectInstance instance : ((ArrowEntityAccessor) context.entity().entity()).ysm$getPotionContents().getAllEffects()) {
                    if (instance.getEffect() == effect) {
                        sum += instance.getAmplifier() + 1;
                        break;
                    }
                }
            } else {
                return null;
            }
        }

        return sum;
    }
}
