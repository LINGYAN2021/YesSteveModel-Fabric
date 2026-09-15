package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.capability.fabric.YsmCapability;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;

public class AuthModelsCapabilityProvider {
    public static final YsmCapability<AuthModelsCapability> AUTH_MODELS_CAP = YsmCapability.register(
            "own_models",
            entity -> entity instanceof Player ? new AuthModelsCapability() : null,
            AuthModelsCapability::serializeNBT,
            (instance, tag) -> instance.deserializeNBT((ListTag) tag));

    private AuthModelsCapabilityProvider() {
    }
}
