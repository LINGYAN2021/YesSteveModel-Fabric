package com.elfmcys.ysm.network;

import net.minecraft.network.FriendlyByteBuf;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.network.protocol.ProtocolVersion;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundFrame(byte[] data) implements CustomPacketPayload {
    public static final Identifier CHANNEL_ID = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, ProtocolVersion.CHANNEL_PATH + "_s2c");
    public static final CustomPacketPayload.Type<ClientboundFrame> TYPE = new CustomPacketPayload.Type<>(CHANNEL_ID);

    public static final StreamCodec<FriendlyByteBuf, ClientboundFrame> CODEC = StreamCodec.of(
            (buffer, frame) -> buffer.writeBytes(frame.data),
            buffer -> {
                byte[] data = new byte[buffer.readableBytes()];
                buffer.readBytes(data);
                return new ClientboundFrame(data);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
