package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.fabric.ClientProtocolGateway;
import net.minecraft.client.player.LocalPlayer;

/**
 * 客户端玩家重生（克隆）事件。Fabric 没有对应事件，
 * 由 {@code ClientPacketListenerMixin} 在 handleRespawn 完成时触发。
 */
public class LocalPlayerRespawnEvent {
    public static void onClone(LocalPlayer oldPlayer, LocalPlayer newPlayer) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        // 仅客户端运行时，执行后续的 copyFrom 会出现 null 值问题
        if (!NetworkHandler.isRemoteChannelPresent()) {
            return;
        }
        var oldCap = EntityCapabilityHolder.peek(oldPlayer, PlayerAnimatableCapabilityProvider.CAP);
        if (oldCap != null) {
            EntityCapabilityHolder.get(newPlayer, PlayerAnimatableCapabilityProvider.CAP)
                    .ifPresent(newCap -> newCap.moveFrom(oldCap));
        }
        ClientProtocolGateway.localPlayerCloned();
    }
}
