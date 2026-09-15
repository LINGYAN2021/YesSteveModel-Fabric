package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.capability.fabric.YsmCapability;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * 收到网络包初始化前，getCapability 返回空
 */
@Environment(EnvType.CLIENT)
public class ProjectileAnimatableCapabilityProvider {
    public static final YsmCapability<ProjectileAnimatableCapability> CAP = YsmCapability.register(
            "projectile_animatable",
            entity -> null);

    private ProjectileAnimatableCapabilityProvider() {
    }

    public static ProjectileAnimatableCapability initialize(Projectile projectile) {
        ProjectileAnimatableCapability existing = EntityCapabilityHolder.peek(projectile, CAP);
        if (existing != null) {
            return existing;
        }
        return EntityCapabilityHolder.put(projectile, CAP, new ProjectileAnimatableCapability(projectile));
    }
}
