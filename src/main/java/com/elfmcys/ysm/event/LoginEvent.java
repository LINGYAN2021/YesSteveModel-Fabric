package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.network.fabric.HandshakeHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class LoginEvent {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (YesSteveModel.isAvailable()) {
                HandshakeHandler.sendServerHello(handler.getPlayer());
            }
        });
    }
}
