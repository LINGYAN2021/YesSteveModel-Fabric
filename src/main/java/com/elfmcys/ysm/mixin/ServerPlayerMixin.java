package com.elfmcys.ysm.mixin;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.event.CapabilityEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("RETURN"))
    private void afterPositionRider(Entity vehicle, boolean force, boolean playRidingSound, CallbackInfoReturnable<Boolean> cir) {
        if (!YesSteveModel.isAvailable() || !cir.getReturnValue()) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        if (vehicle.getFirstPassenger() == serverPlayer) {
            CapabilityEvent.onVehicleSetModel(vehicle, serverPlayer);
        }
    }
}
