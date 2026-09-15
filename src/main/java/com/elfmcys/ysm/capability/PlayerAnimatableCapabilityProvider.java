package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.capability.fabric.YsmCapability;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;

@Environment(EnvType.CLIENT)
public class PlayerAnimatableCapabilityProvider {
    public static final YsmCapability<PlayerAnimatableCapability> CAP = YsmCapability.register(
            "animatable",
            entity -> entity.level().isClientSide() && entity instanceof AbstractClientPlayer player
                    ? new PlayerAnimatableCapability(player) : null);

    private PlayerAnimatableCapabilityProvider() {
    }
}
