package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.capability.fabric.YsmCapability;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

/**
 * 收到网络包初始化前，getCapability 返回空
 */
@Environment(EnvType.CLIENT)
public class VehicleAnimatableCapabilityProvider {
    public static final YsmCapability<VehicleAnimatableCapability> CAP = YsmCapability.register(
            "vehicle_animatable",
            entity -> null);

    private VehicleAnimatableCapabilityProvider() {
    }

    public static VehicleAnimatableCapability initialize(Entity entity) {
        VehicleAnimatableCapability existing = EntityCapabilityHolder.peek(entity, CAP);
        if (existing != null) {
            return existing;
        }
        return EntityCapabilityHolder.put(entity, CAP, new VehicleAnimatableCapability(entity));
    }
}
