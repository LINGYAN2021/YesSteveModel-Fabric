package com.elfmcys.ysm.mixin;

import com.elfmcys.ysm.event.MobEffectSyncEvent;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEffectMixin {
    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("RETURN"))
    private void ysm$onEffectAdded(MobEffectInstance effectInstance, Entity source,
                                   CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            MobEffectSyncEvent.onAdded((LivingEntity) (Object) this, effectInstance);
        }
    }

    @Inject(method = "removeEffectNoUpdate", at = @At("RETURN"))
    private void ysm$onEffectRemoved(Holder<MobEffect> effect,
                                     CallbackInfoReturnable<MobEffectInstance> cir) {
        if (cir.getReturnValue() != null) {
            MobEffectSyncEvent.onRemoved((LivingEntity) (Object) this, effect);
        }
    }
}
