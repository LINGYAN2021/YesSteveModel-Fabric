package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.capability.fabric.YsmCapability;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;

public class StarModelsCapabilityProvider {
    public static final YsmCapability<StarModelsCapability> STAR_MODELS_CAP = YsmCapability.register(
            "star_models",
            entity -> entity instanceof Player ? new StarModelsCapability() : null,
            StarModelsCapability::serializeNBT,
            (instance, tag) -> instance.deserializeNBT((ListTag) tag));

    private StarModelsCapabilityProvider() {
    }
}
