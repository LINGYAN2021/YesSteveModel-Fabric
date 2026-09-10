package com.elfmcys.ysm.network.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric 侧数据包处理上下文，对齐原 Forge NetworkEvent.Context 的使用面
 */
public final class YsmNetContext {
    private final @Nullable MinecraftServer server;
    private final @Nullable ServerPlayer sender;
    private final @Nullable Minecraft client;
    private final Connection connection;

    private YsmNetContext(@Nullable MinecraftServer server, @Nullable ServerPlayer sender,
                          @Nullable Minecraft client, Connection connection) {
        this.server = server;
        this.sender = sender;
        this.client = client;
        this.connection = connection;
    }

    public static YsmNetContext server(MinecraftServer server, ServerPlayer sender) {
        Connection connection = ((com.elfmcys.ysm.mixin.ServerCommonPacketListenerAccessor) sender.connection).ysm$getConnection();
        return new YsmNetContext(server, sender, null, connection);
    }

    public static YsmNetContext client(Minecraft client) {
        var connection = client.getConnection();
        if (connection == null) {
            throw new IllegalStateException("Client packet listener is unavailable");
        }
        return new YsmNetContext(null, null, client, connection.getConnection());
    }

    public @Nullable ServerPlayer sender() {
        return sender;
    }

    public Connection connection() {
        return connection;
    }

    public void enqueueWork(Runnable runnable) {
        if (server != null) {
            server.execute(runnable);
        } else if (client != null) {
            client.execute(runnable);
        }
    }

    public void disconnect(Component reason) {
        connection.disconnect(reason);
    }
}
