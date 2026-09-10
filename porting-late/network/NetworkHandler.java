package com.elfmcys.ysm.network;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.network.fabric.AssetTransferHandler;
import com.elfmcys.ysm.network.fabric.ClientboundEnvelope;
import com.elfmcys.ysm.network.fabric.ControlHandler;
import com.elfmcys.ysm.network.fabric.HandshakeHandler;
import com.elfmcys.ysm.network.fabric.MinecraftStateHandler;
import com.elfmcys.ysm.network.fabric.PlayerStateHandler;
import com.elfmcys.ysm.network.fabric.ServerboundEnvelope;
import com.elfmcys.ysm.network.fabric.YsmConnectionData;
import com.elfmcys.ysm.network.fabric.YsmEnvelope;
import com.elfmcys.ysm.network.fabric.YsmMessageBinding;
import com.elfmcys.ysm.network.fabric.YsmNetContext;
import com.elfmcys.ysm.network.fabric.YsmProtocolRegistry;
import com.elfmcys.ysm.network.fabric.YsmProtoCodec;
import com.elfmcys.ysm.network.protocol.MessageDirection;
import com.elfmcys.ysm.network.protocol.PeerProtocolProfile;
import com.elfmcys.ysm.network.protocol.ProtocolMessageSpec;
import com.elfmcys.ysm.network.protocol.ProtocolVersion;
import com.elfmcys.ysm.network.protocol.ProtocolMessages;
import com.elfmcys.ysm.proto.network.protocol.v0.AssetTransferV0;
import com.elfmcys.ysm.proto.network.protocol.v0.ControlV0;
import com.elfmcys.ysm.proto.network.protocol.v0.HandshakeV0;
import com.elfmcys.ysm.proto.network.protocol.v0.PlayerStateV0;
import com.elfmcys.ysm.proto.network.protocol.v0.minecraft.MinecraftStateV0;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import us.hebi.quickbuf.ProtoMessage;

import java.util.Optional;
import java.util.function.BiConsumer;

public final class NetworkHandler {
    public static final String VERSION = ProtocolVersion.TRANSPORT_VERSION;
    public static final Identifier CHANNEL_NAME =
            Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, ProtocolVersion.CHANNEL_PATH);
    public static final CustomPacketPayload.Type<ServerboundFrame> SERVERBOUND_TYPE = ServerboundFrame.TYPE;
    public static final CustomPacketPayload.Type<ClientboundFrame> CLIENTBOUND_TYPE = ClientboundFrame.TYPE;
    private static final String ATTRIBUTE_CHANNEL_VERSION = YesSteveModel.MOD_ID + "_channel_version";
    private static final String ATTRIBUTE_PEER_PROFILE = YesSteveModel.MOD_ID + "_peer_protocol_profile";

    private NetworkHandler() {
    }

    public static boolean setChannelVersion(Connection connection, String channelVersion) {
        return ((YsmConnectionData) connection).ysm$attributes()
                .putIfAbsent(ATTRIBUTE_CHANNEL_VERSION, channelVersion) == null;
    }

    public static boolean setPeerProfile(Connection connection, PeerProtocolProfile profile) {
        return ((YsmConnectionData) connection).ysm$attributes()
                .putIfAbsent(ATTRIBUTE_PEER_PROFILE, profile) == null;
    }

    public static Optional<PeerProtocolProfile> peerProfile(@Nullable Connection connection) {
        if (connection == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((PeerProtocolProfile) ((YsmConnectionData) connection)
                .ysm$attributes().get(ATTRIBUTE_PEER_PROFILE));
    }

    public static boolean isPlayerChannelPresent(ServerPlayer player) {
        return player.connection != null && isChannelPresent(player.connection.getConnection());
    }

    public static boolean isRemoteChannelPresent() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null && isChannelPresent(connection.getConnection());
    }

    public static boolean isChannelPresent(@Nullable Connection connection) {
        return connection != null && VERSION.equals(((YsmConnectionData) connection)
                .ysm$attributes().get(ATTRIBUTE_CHANNEL_VERSION));
    }

    public static void init() {
        registerProto(HandshakeV0.ClientHello.class, HandshakeV0.ClientHello::parseFrom,
                HandshakeHandler::handleClientHello);
        registerProto(HandshakeV0.ServerHello.class, HandshakeV0.ServerHello::parseFrom,
                HandshakeHandler::handleServerHello);
        registerProto(PlayerStateV0.PlayerStateReport.class, PlayerStateV0.PlayerStateReport::parseFrom,
                PlayerStateHandler::handleReport);
        registerProto(PlayerStateV0.PlayerStateUpdate.class, PlayerStateV0.PlayerStateUpdate::parseFrom,
                PlayerStateHandler::handleUpdate);
        registerProto(ControlV0.SelectModelRequest.class, ControlV0.SelectModelRequest::parseFrom,
                ControlHandler::handleSelectModel);
        registerProto(ControlV0.AuthorizedModelsSnapshot.class, ControlV0.AuthorizedModelsSnapshot::parseFrom,
                ControlHandler::handleAuthorizedModels);
        registerProto(ControlV0.StarredModelsSnapshot.class, ControlV0.StarredModelsSnapshot::parseFrom,
                ControlHandler::handleStarredModels);
        registerProto(ControlV0.UpdateStarredModelRequest.class, ControlV0.UpdateStarredModelRequest::parseFrom,
                ControlHandler::handleUpdateStar);
        registerProto(ControlV0.EntityAnimationActionRequest.class, ControlV0.EntityAnimationActionRequest::parseFrom,
                ControlHandler::handleEntityAnimation);
        registerProto(ControlV0.ExecuteMolangEvent.class, ControlV0.ExecuteMolangEvent::parseFrom,
                ControlHandler::handleExecuteMolang);
        registerProto(ControlV0.SubmitRouletteExpressionRequest.class,
                ControlV0.SubmitRouletteExpressionRequest::parseFrom, ControlHandler::handleSubmitRoulette);
        registerProto(ControlV0.EmitMolangSync.class, ControlV0.EmitMolangSync::parseFrom,
                ControlHandler::handleEmitMolangSync);
        registerProto(ControlV0.MolangSyncEvent.class, ControlV0.MolangSyncEvent::parseFrom,
                ControlHandler::handleMolangSync);
        registerProto(ControlV0.SwingHandRequest.class, ControlV0.SwingHandRequest::parseFrom,
                ControlHandler::handleSwingHand);
        registerProto(MinecraftStateV0.ProjectileModelState.class, MinecraftStateV0.ProjectileModelState::parseFrom,
                MinecraftStateHandler::handleProjectile);
        registerProto(MinecraftStateV0.VehicleModelState.class, MinecraftStateV0.VehicleModelState::parseFrom,
                MinecraftStateHandler::handleVehicle);
        register(AssetTransferV0.AssetFragment.class, AssetTransferV0.AssetFragment::parseFrom,
                AssetTransferHandler::handleFragmentPayload);
        registerProto(AssetTransferV0.ModelAssetBatchRequest.class, AssetTransferV0.ModelAssetBatchRequest::parseFrom,
                AssetTransferHandler::handleBatchRequest);
        registerProto(AssetTransferV0.ModelAssetBatchFailure.class, AssetTransferV0.ModelAssetBatchFailure::parseFrom,
                AssetTransferHandler::handleBatchFailure);
        registerProto(AssetTransferV0.CatalogResyncRequest.class, AssetTransferV0.CatalogResyncRequest::parseFrom,
                AssetTransferHandler::handleCatalogResync);
        registerProto(AssetTransferV0.ModelAssetBatchCancel.class, AssetTransferV0.ModelAssetBatchCancel::parseFrom,
                AssetTransferHandler::handleBatchCancel);
        registerProto(AssetTransferV0.AssetTransferRelease.class, AssetTransferV0.AssetTransferRelease::parseFrom,
                AssetTransferHandler::handleTransferRelease);

        PayloadTypeRegistry.serverboundPlay().register(SERVERBOUND_TYPE, ServerboundFrame.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CLIENTBOUND_TYPE, ClientboundFrame.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SERVERBOUND_TYPE, (payload, context) -> {
            YsmEnvelope envelope = decode(payload.data(), MessageDirection.CLIENT_TO_SERVER);
            envelope.handle(YsmNetContext.server(context.server(), context.player()));
        });
    }

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(CLIENTBOUND_TYPE, (payload, context) -> {
            YsmEnvelope envelope = decode(payload.data(), MessageDirection.SERVER_TO_CLIENT);
            envelope.handle(YsmNetContext.client(context.client()));
        });
    }

    private static YsmEnvelope decode(byte[] data, MessageDirection direction) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        try {
            return YsmProtoCodec.decode(buffer, direction);
        } finally {
            buffer.release();
        }
    }

    private static <T extends ProtoMessage<T>> void registerProto(
            Class<T> type, YsmProtoCodec.Parser<T> parser,
            BiConsumer<T, YsmNetContext> handler) {
        register(type, parser, (payload, context) -> {
            try (payload) {
                if (payload.raw().isPresent()) {
                    throw new IllegalArgumentException("Raw attachment is not valid for " + type.getName());
                }
                handler.accept(payload.protobuf(), context);
            }
        });
    }

    private static <T extends ProtoMessage<T>> void register(
            Class<T> type, YsmProtoCodec.Parser<T> parser,
            BiConsumer<NetworkPayload<T>, YsmNetContext> handler) {
        ProtocolMessageSpec<T> spec = ProtocolMessages.REGISTRY.find(type).orElseThrow();
        YsmProtocolRegistry.add(new YsmMessageBinding<>(spec, parser, handler));
    }

    public static void sendToServer(ProtoMessage<?> message) {
        sendToServer(payload(message));
    }

    public static void sendToServer(NetworkPayload<?> payload) {
        if (!isRemoteChannelPresent()) {
            payload.close();
            return;
        }
        ClientPlayNetworking.send(new ServerboundFrame(encode(serverbound(payload))));
    }

    public static void sendToClientPlayer(ProtoMessage<?> message, Player player) {
        sendToClientPlayer(payload(message), player);
    }

    public static void sendToClientPlayer(NetworkPayload<?> payload, Player player) {
        ServerPlayNetworking.send((ServerPlayer) player,
                new ClientboundFrame(encode(clientbound(payload))));
    }

    public static void broadcastToAllPlayers(ProtoMessage<?> message, MinecraftServer server) {
        ClientboundFrame frame = new ClientboundFrame(encode(clientbound(payload(message))));
        for (ServerPlayer player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, frame);
        }
    }

    public static void broadcastToVisiblePlayers(ProtoMessage<?> message, Entity centerEntity) {
        ClientboundFrame frame = new ClientboundFrame(encode(clientbound(payload(message))));
        for (ServerPlayer player : PlayerLookup.tracking(centerEntity)) {
            ServerPlayNetworking.send(player, frame);
        }
    }

    public static void broadcastToVisiblePlayersAndSelf(ProtoMessage<?> message, Player self) {
        ClientboundFrame frame = new ClientboundFrame(encode(clientbound(payload(message))));
        var tracking = PlayerLookup.tracking(self);
        for (ServerPlayer player : tracking) {
            ServerPlayNetworking.send(player, frame);
        }
        if (self instanceof ServerPlayer serverPlayer && !tracking.contains(serverPlayer)) {
            ServerPlayNetworking.send(serverPlayer, frame);
        }
    }

    private static byte[] encode(YsmEnvelope envelope) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            YsmProtoCodec.encode(envelope, buffer);
            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);
            return data;
        } finally {
            buffer.release();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static NetworkPayload<?> payload(ProtoMessage<?> message) {
        return NetworkPayload.protobuf((ProtoMessage) message);
    }

    private static ServerboundEnvelope serverbound(NetworkPayload<?> payload) {
        return new ServerboundEnvelope(binding(payload, MessageDirection.CLIENT_TO_SERVER), payload, true);
    }

    private static ClientboundEnvelope clientbound(NetworkPayload<?> payload) {
        return new ClientboundEnvelope(binding(payload, MessageDirection.SERVER_TO_CLIENT), payload, true);
    }

    private static YsmMessageBinding<?> binding(NetworkPayload<?> payload, MessageDirection direction) {
        var spec = ProtocolMessages.REGISTRY.find(payload.protobuf().getClass())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unregistered protocol message type: " + payload.protobuf().getClass().getName()));
        if (spec.direction() != direction) {
            throw new IllegalArgumentException("Protocol message has the wrong network direction");
        }
        return YsmProtocolRegistry.find(spec.id(), direction).orElseThrow();
    }
}
