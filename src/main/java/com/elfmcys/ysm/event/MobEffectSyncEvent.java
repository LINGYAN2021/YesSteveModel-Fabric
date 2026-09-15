package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.ModelInfoCapabilityProvider;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;

/**
 * 药水效果同步，由 LivingEntityEffectMixin 触发
 */
public final class MobEffectSyncEvent {
    private MobEffectSyncEvent() {
    }

    public static void onAdded(LivingEntity entity, MobEffectInstance effectInstance) {
        if (!YesSteveModel.isAvailable() || entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof ServerPlayer player && effectInstance.getEffect() != null) {
            EntityCapabilityHolder.get(player, ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap ->
                    cap.getPropertiesTracker().addEffect(player, effectInstance.getEffect().value(),
                            effectInstance.getAmplifier() + 1));
        }
    }

    public static void onRemoved(LivingEntity entity, Holder<MobEffect> effect) {
        if (!YesSteveModel.isAvailable() || entity.level().isClientSide() || effect == null) {
            return;
        }
        if (entity instanceof ServerPlayer player) {
            EntityCapabilityHolder.get(player, ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap ->
                    cap.getPropertiesTracker().removeEffect(player, effect.value()));
        }
    }
}
