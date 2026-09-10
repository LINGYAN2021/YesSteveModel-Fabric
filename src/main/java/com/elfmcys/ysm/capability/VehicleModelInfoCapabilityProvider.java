package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.capability.fabric.YsmCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

public class VehicleModelInfoCapabilityProvider {
    public static final YsmCapability<VehicleModelInfoCapability> CAP = YsmCapability.register(
            "vehicle_model_id",
            entity -> !entity.level().isClientSide() && !(entity instanceof Player) && !(entity instanceof Projectile)
                    ? new VehicleModelInfoCapability() : null,
            VehicleModelInfoCapability::serializeNBT,
            (instance, tag) -> instance.deserializeNBT((CompoundTag) tag));

    private VehicleModelInfoCapabilityProvider() {
    }
}
