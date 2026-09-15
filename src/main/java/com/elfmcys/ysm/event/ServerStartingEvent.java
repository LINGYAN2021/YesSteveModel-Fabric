package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.model.server.ServerModelService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class ServerStartingEvent {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (YesSteveModel.isAvailable()) {
                ServerModelService.start(server);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
                ServerModelService.current().ifPresent(ServerModelService::close));
    }
}
