package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.model.ClientModelService;
import com.elfmcys.ysm.client.sound.decoder.DecoderManager;
import com.elfmcys.ysm.client.texture.CustomTextureManager;
import com.elfmcys.ysm.network.fabric.ClientProtocolGateway;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class ClientTickEvent {
    private static int tickCount;
    private static int refreshRate = 60;

    public static void register() {
        // 原 Forge 逻辑在 Phase.START 时执行
        ClientTickEvents.START_CLIENT_TICK.register(client -> onClientTick());
    }

    public static void onClientTick() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        tickCount++;
        CustomTextureManager.tick();
        ClientModelService.current().ifPresent(ClientModelService::tick);
        DecoderManager.tick();
        refreshRate = Minecraft.getInstance().getWindow().getRefreshRate();

        var player = Minecraft.getInstance().player;
        if (player != null) {
            EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(capability -> {
                capability.handleRoamingVarsChanges();
                ClientProtocolGateway.tick(player, capability);
            });
        }
    }

    public static int getTickCount() {
        return tickCount;
    }

    public static int getRefreshRate() {
        return refreshRate;
    }
}
