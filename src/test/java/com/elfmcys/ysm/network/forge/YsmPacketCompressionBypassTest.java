package com.elfmcys.ysm.network.forge;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraftforge.network.ICustomPacket;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SuppressWarnings("removal")
class YsmPacketCompressionBypassTest {
    private static final Identifier YSM_CHANNEL =
            Identifier.fromNamespaceAndPath("ysm", "network");

    @Test
    void markedPacketUsesMinecraftUncompressedEnvelope() {
        var expected = new byte[512];
        java.util.Arrays.fill(expected, (byte) 3);
        var channel = channel(expected);

        channel.writeOutbound(new TestCustomPacket(YSM_CHANNEL));
        var encoded = new FriendlyByteBuf(channel.readOutbound());
        try {
            assertNotEquals(0, encoded.readableBytes());
            org.junit.jupiter.api.Assertions.assertEquals(0, encoded.readVarInt());
            var actual = new byte[encoded.readableBytes()];
            encoded.readBytes(actual);
            assertArrayEquals(expected, actual);
        } finally {
            encoded.release();
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void unmarkedPacketStillUsesMinecraftZlib() {
        var channel = channel(new byte[512]);
        channel.writeOutbound(new TestCustomPacket(Identifier.fromNamespaceAndPath("other", "channel")));
        ByteBuf output = channel.readOutbound();
        var encoded = new FriendlyByteBuf(output);
        try {
            assertNotEquals(0, encoded.readVarInt());
        } finally {
            encoded.release();
            channel.finishAndReleaseAll();
        }
    }

    private static EmbeddedChannel channel(byte[] payload) {
        return new EmbeddedChannel(
                new YsmPacketCompressionBypass.Encoder(1),
                new TestPacketEncoder(payload),
                new YsmPacketCompressionBypass.Marker(YSM_CHANNEL));
    }

    private record TestCustomPacket(Identifier name) implements ICustomPacket<Packet<?>> {
        @Override
        public FriendlyByteBuf getInternalData() {
            return null;
        }

        @Override
        public Identifier getName() {
            return name;
        }

        @Override
        public int getIndex() {
            return 0;
        }
    }

    private static final class TestPacketEncoder extends MessageToMessageEncoder<TestCustomPacket> {
        private final byte[] payload;

        private TestPacketEncoder(byte[] payload) {
            this.payload = payload;
        }

        @Override
        protected void encode(ChannelHandlerContext context, TestCustomPacket message,
                              List<Object> output) {
            output.add(Unpooled.wrappedBuffer(payload));
        }
    }
}
