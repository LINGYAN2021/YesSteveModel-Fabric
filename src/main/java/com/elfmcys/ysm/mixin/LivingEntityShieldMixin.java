package com.elfmcys.ysm.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityShieldMixin implements com.elfmcys.ysm.event.ShieldCooldownAccess {
    @Unique
    private int ysm$shieldBlockCooldown;

    @Inject(method = "blockedByItem", at = @At("HEAD"))
    private void ysm$onBlockedByItem(LivingEntity attacker, CallbackInfo ci) {
        ysm$shieldBlockCooldown = 5;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void ysm$tickShieldCooldown(CallbackInfo ci) {
        if (ysm$shieldBlockCooldown > 0) {
            ysm$shieldBlockCooldown--;
        }
    }

    @Override
    public int ysm$shieldBlockCooldown() {
        return ysm$shieldBlockCooldown;
    }
}
