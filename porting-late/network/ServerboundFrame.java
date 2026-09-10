package com.elfmcys.ysm.network;

import net.minecraft.network.FriendlyByteBuf;
import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.network.protocol.ProtocolVersion;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerboundFrame(byte[] data) implements CustomPacketPayload {
    public static final Identifier CHANNEL_ID = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, ProtocolVersion.CHANNEL_PATH + "_c2s");
    public static final CustomPacketPayload.Type<ServerboundFrame> TYPE = new CustomPacketPayload.Type<>(CHANNEL_ID);

    public static final StreamCodec<FriendlyByteBuf, ServerboundFrame> CODEC = StreamCodec.of(
            (buffer, frame) -> buffer.writeBytes(frame.data),
            buffer -> {
                byte[] data = new byte[buffer.readableBytes()];
                buffer.readBytes(data);
                return new ServerboundFrame(data);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
