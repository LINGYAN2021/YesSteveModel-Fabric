package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.capability.fabric.YsmCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.Projectile;

public class ProjectileModelInfoCapabilityProvider {
    public static final YsmCapability<ProjectileModelInfoCapability> CAP = YsmCapability.register(
            "projectile_model_id",
            entity -> !entity.level().isClientSide() && entity instanceof Projectile ? new ProjectileModelInfoCapability() : null,
            ProjectileModelInfoCapability::serializeNBT,
            (instance, tag) -> instance.deserializeNBT((CompoundTag) tag));

    private ProjectileModelInfoCapabilityProvider() {
    }
}
