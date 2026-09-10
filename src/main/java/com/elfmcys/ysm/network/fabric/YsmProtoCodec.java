package com.elfmcys.ysm.network.fabric;

import com.elfmcys.ysm.buffer.ArrayBuffer;
import com.elfmcys.ysm.buffer.BufferType;
import com.elfmcys.ysm.buffer.UniBuffer;
import com.elfmcys.ysm.natives.Zstd;
import com.elfmcys.ysm.network.NetworkPayload;
import com.elfmcys.ysm.network.protocol.MessageDirection;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import us.hebi.quickbuf.ProtoMessage;
import us.hebi.quickbuf.ProtoSink;
import us.hebi.quickbuf.ProtoSource;

import java.io.IOException;

/** Quickbuf adapter for the compact/simple and feature-complete YSM frames. */
public final class YsmProtoCodec {
    static final int COMPRESSION_THRESHOLD = 64;
    static final int DYNAMIC_COMPRESSION_LEVEL = 10;
    static final int FLAG_PROTOBUF_ZSTD = 1;
    static final int FLAG_RAW_TAIL = 1 << 1;
    private static final int KNOWN_FLAGS = FLAG_PROTOBUF_ZSTD | FLAG_RAW_TAIL;

    private YsmProtoCodec() {
    }

    static PreparedProtobuf prepare(ProtoMessage<?> message, int maxDecodedBytes) {
        var decodedSize = message.getSerializedSize();
        if (decodedSize > maxDecodedBytes) {
            throw new EncoderException("Protobuf message exceeds its registered size limit: " + decodedSize);
        }
        ArrayBuffer serialized = ArrayBuffer.allocate(decodedSize);
        try {
            message.writeTo(ProtoSink.newInstance(
                    serialized.array(), serialized.arrayOffset(), serialized.size()));
            if (decodedSize < COMPRESSION_THRESHOLD) {
                return new PreparedProtobuf(ProtobufEncoding.RAW, decodedSize, serialized);
            }
            UniBuffer compressed = Zstd.compressAndHash(
                    serialized, null, BufferType.NATIVE, DYNAMIC_COMPRESSION_LEVEL);
            if (compressed.size() >= decodedSize) {
                compressed.close();
                return new PreparedProtobuf(ProtobufEncoding.RAW, decodedSize, serialized);
            }
            serialized.close();
            return new PreparedProtobuf(ProtobufEncoding.ZSTD, decodedSize, compressed);
        } catch (IOException | RuntimeException error) {
            serialized.close();
            throw new EncoderException("Failed to encode protobuf message", error);
        }
    }

    public static void encode(YsmEnvelope envelope, FriendlyByteBuf target) {
        try (envelope) {
            var protobuf = envelope.encoded();
            var raw = envelope.payload().raw().orElse(null);
            var full = protobuf.encoding() == ProtobufEncoding.ZSTD || raw != null;
            target.writeVarInt((envelope.binding().spec().id() << 1) | (full ? 1 : 0));
            if (!full) {
                UniBufferIO.write(target, protobuf.wire());
                return;
            }
            var flags = protobuf.encoding() == ProtobufEncoding.ZSTD ? FLAG_PROTOBUF_ZSTD : 0;
            if (raw != null) {
                flags |= FLAG_RAW_TAIL;
            }
            target.writeVarInt(flags);
            target.writeVarInt(protobuf.wire().size());
            if (protobuf.encoding() == ProtobufEncoding.ZSTD) {
                target.writeVarInt(protobuf.decodedSize());
            }
            UniBufferIO.write(target, protobuf.wire());
            if (raw != null) {
                UniBufferIO.write(target, raw);
            }
        }
    }

    public static YsmEnvelope decode(FriendlyByteBuf source, MessageDirection direction) {
        var frameTag = source.readVarInt();
        if (frameTag < 0) {
            throw new DecoderException("Negative YSM frame tag");
        }
        var full = (frameTag & 1) != 0;
        var messageId = frameTag >>> 1;
        var binding = YsmProtocolRegistry.find(messageId, direction)
                .orElseThrow(() -> new DecoderException("Unknown YSM protocol message id: " + messageId));
        if (!full) {
            var size = source.readableBytes();
            checkDecodedSize(size, binding.spec().maxEncodedBytes());
            var protobuf = parse(source, size, size, false, binding);
            return YsmEnvelope.inbound(binding, payload(protobuf, null));
        }

        var flags = source.readVarInt();
        if ((flags & ~KNOWN_FLAGS) != 0) {
            throw new DecoderException("Unknown YSM full-frame flags: " + flags);
        }
        var wireSize = source.readVarInt();
        if (wireSize < 0 || wireSize > source.readableBytes()) {
            throw new DecoderException("Invalid YSM protobuf wire size: " + wireSize);
        }
        var compressed = (flags & FLAG_PROTOBUF_ZSTD) != 0;
        var decodedSize = compressed ? source.readVarInt() : wireSize;
        checkDecodedSize(decodedSize, binding.spec().maxEncodedBytes());
        if (wireSize > source.readableBytes()) {
            throw new DecoderException("Truncated YSM protobuf payload");
        }
        var protobuf = parse(source, wireSize, decodedSize, compressed, binding);
        var hasRaw = (flags & FLAG_RAW_TAIL) != 0;
        if (!hasRaw && source.isReadable()) {
            throw new DecoderException("YSM full frame contains an undeclared raw tail");
        }
        if (!hasRaw) {
            return YsmEnvelope.inbound(binding, payload(protobuf, null));
        }
        return YsmEnvelope.inbound(binding,
                payload(protobuf, UniBufferIO.readNative(source, source.readableBytes())));
    }

    private static ProtoMessage<?> parse(FriendlyByteBuf source, int wireSize, int decodedSize,
                                         boolean compressed, YsmMessageBinding<?> binding) {
        try (var wire = UniBufferIO.readNative(source, wireSize)) {
            if (compressed) {
                try (var decoded = Zstd.decompressAndValidate(
                        wire, decodedSize, null, BufferType.ARRAY)) {
                    if (!(decoded instanceof ArrayBuffer array)) {
                        throw new DecoderException("Zstd returned a non-array protobuf buffer");
                    }
                    return binding.parser().parse(ProtoSource.newInstance(
                            array.array(), array.arrayOffset(), array.size()));
                }
            }
            try (var array = wire.acquireArray()) {
                return binding.parser().parse(ProtoSource.newInstance(
                        array.array(), array.arrayOffset(), array.size()));
            }
        } catch (IOException | RuntimeException error) {
            throw new DecoderException("Failed to decode YSM protobuf message", error);
        }
    }

    private static void checkDecodedSize(int size, int maximum) {
        if (size < 0 || size > maximum) {
            throw new DecoderException("Invalid YSM protobuf decoded size: " + size);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static NetworkPayload<?> payload(ProtoMessage<?> protobuf, UniBuffer raw) {
        return raw == null
                ? NetworkPayload.protobuf((ProtoMessage) protobuf)
                : NetworkPayload.withRaw((ProtoMessage) protobuf, raw);
    }

    @FunctionalInterface
    public interface Parser<T extends ProtoMessage<T>> {
        T parse(ProtoSource data) throws IOException;
    }

    enum ProtobufEncoding {
        RAW,
        ZSTD
    }

    record PreparedProtobuf(ProtobufEncoding encoding, int decodedSize,
                            UniBuffer wire) implements AutoCloseable {
        @Override
        public void close() {
            wire.close();
        }
    }
}
