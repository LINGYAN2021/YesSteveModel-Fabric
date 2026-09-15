package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.model.server.ServerModelService;
import com.elfmcys.ysm.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class LoggedOutEvent {
    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (!YesSteveModel.isAvailable()) {
                return;
            }
            var serverPlayer = handler.getPlayer();
            if (NetworkHandler.isPlayerChannelPresent(serverPlayer)) {
                ServerModelService.current().ifPresent(service -> service.playerDisconnected(serverPlayer.getUUID()));
            }
        });
    }
}
