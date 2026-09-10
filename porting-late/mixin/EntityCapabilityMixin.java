package com.elfmcys.ysm.mixin;

import com.elfmcys.ysm.capability.fabric.EntityCapabilityStore;
import com.elfmcys.ysm.capability.fabric.YsmCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(Entity.class)
public abstract class EntityCapabilityMixin implements EntityCapabilityStore {
    @Unique
    private final Map<YsmCapability<?>, Object> ysm$capabilities = new IdentityHashMap<>();

    @Override
    public Map<YsmCapability<?>, Object> ysm$capabilityMap() {
        return ysm$capabilities;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void ysm$saveCapabilities(ValueOutput output, CallbackInfo ci) {
        YsmCapability.saveAll((Entity) (Object) this, output);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void ysm$loadCapabilities(ValueInput input, CallbackInfo ci) {
        YsmCapability.loadAll((Entity) (Object) this, input);
    }
}
