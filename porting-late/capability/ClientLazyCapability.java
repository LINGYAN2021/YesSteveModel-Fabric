package com.elfmcys.ysm.capability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.Nullable;

/**
 * 客户端实体上的懒加载 capability 入口，网络包到达时才真正初始化 animatable
 */
public class ClientLazyCapability {
    private final Entity entity;

    public ClientLazyCapability(Entity entity) {
        this.entity = entity;
    }

    public VehicleAnimatableCapability initializeVehicleAnimatable() {
        return VehicleAnimatableCapabilityProvider.initialize(entity);
    }

    public @Nullable ProjectileAnimatableCapability initializeProjectileAnimatable() {
        if (entity instanceof Projectile projectile) {
            return ProjectileAnimatableCapabilityProvider.initialize(projectile);
        }
        return null;
    }
}
