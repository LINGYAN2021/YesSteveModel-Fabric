package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.model.ClientModelService;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.fabric.ClientSessionRuntime;
import com.elfmcys.ysm.network.session.ActiveSessionMode;
import com.elfmcys.ysm.network.session.SessionMode;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ClientLoggedEvent {
    private static boolean LOGGED_IN = false;

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onPlayerLoggedIn(handler));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onPlayerLoggedOut());
    }

    public static void onPlayerLoggedIn(ClientPacketListener connection) {
        if (LOGGED_IN) {
            return;
        }
        if (!YesSteveModel.isAvailable()) {
            LOGGED_IN = true;
            YesSteveModel.sendUnavailableMessage();
            return;
        }
        try {
            ClientModelService.instance().awaitBuiltinReadiness();
        } catch (RuntimeException error) {
            YesSteveModel.LOGGER.error(
                    "Builtin models were not ready before entering the level", error);
            connection.getConnection().disconnect(Component.translatable(
                    "disconnect.yes_steve_model.builtin_initialization_failed"));
            return;
        }
        var connectionGeneration = ClientSessionRuntime.beginConnection();
        CompletableFuture.runAsync(
                () -> Minecraft.getInstance().execute(
                        () -> ClientSessionRuntime.completeGameServerDetection(connectionGeneration)),
                CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS));
        LOGGED_IN = true;

        var notifyThread = new Thread(() -> {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ignored) {
                return;
            }
            Minecraft.getInstance().execute(() -> {
                var player = Minecraft.getInstance().player;
                var session = ClientSessionRuntime.snapshot();
                if (player != null && player.connection.isAcceptingMessages()
                        && session.requestedMode() == SessionMode.AUTO
                        && session.activeMode().orElse(null) == ActiveSessionMode.LOCAL_ONLY
                        && !NetworkHandler.isChannelPresent(player.connection.getConnection())) {
                    player.sendSystemMessage(Component.translatable("message.yes_steve_model.client.server_not_found"));
                }
            });
        });
        notifyThread.setDaemon(true);
        notifyThread.start();
    }

    public static void onPlayerLoggedOut() {
        if (!LOGGED_IN) {
            return;
        }
        LOGGED_IN = false;
        if (YesSteveModel.isAvailable()) {
            ClientSessionRuntime.disconnect();
        }
    }
}
