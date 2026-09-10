package com.elfmcys.ysm.event;

import net.minecraft.world.entity.LivingEntity;

/**
 * 盾牌格挡冷却查询，状态由 LivingEntityShieldMixin 维护
 */
public final class LivingShieldBlockEvent {
    private LivingShieldBlockEvent() {
    }

    public static boolean inShieldBlockCooldown(LivingEntity entity) {
        return ((ShieldCooldownAccess) entity).ysm$shieldBlockCooldown() > 0;
    }
}
