package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.capability.fabric.YsmCapability;
import net.minecraft.world.entity.player.Player;

public class ModelInfoCapabilityProvider {
    public static final YsmCapability<ModelInfoCapability> MODEL_INFO_CAP = YsmCapability.register(
            "model_id",
            entity -> !entity.level().isClientSide() && entity instanceof Player ? new ModelInfoCapability() : null,
            ModelInfoCapability::serializeNBT,
            (instance, tag) -> instance.deserializeNBT((net.minecraft.nbt.CompoundTag) tag));

    private ModelInfoCapabilityProvider() {
    }
}
