package com.elfmcys.ysm.network.fabric;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.network.NetworkPayload;

public abstract class YsmEnvelope implements AutoCloseable {
    private final YsmMessageBinding<?> binding;
    private final NetworkPayload<?> payload;
    private final YsmProtoCodec.PreparedProtobuf encoded;

    protected YsmEnvelope(YsmMessageBinding<?> binding, NetworkPayload<?> payload, boolean outbound) {
        this.binding = binding;
        this.payload = payload;
        this.encoded = prepare(binding, payload, outbound);
    }

    private static YsmProtoCodec.PreparedProtobuf prepare(
            YsmMessageBinding<?> binding, NetworkPayload<?> payload, boolean outbound) {
        if (!outbound) {
            return null;
        }
        try {
            return YsmProtoCodec.prepare(payload.protobuf(), binding.spec().maxEncodedBytes());
        } catch (Throwable error) {
            payload.close();
            throw error;
        }
    }

    static YsmEnvelope inbound(YsmMessageBinding<?> binding, NetworkPayload<?> payload) {
        return binding.spec().direction() == com.elfmcys.ysm.network.protocol.MessageDirection.CLIENT_TO_SERVER
                ? new ServerboundEnvelope(binding, payload, false)
                : new ClientboundEnvelope(binding, payload, false);
    }

    YsmMessageBinding<?> binding() {
        return binding;
    }

    NetworkPayload<?> payload() {
        return payload;
    }

    YsmProtoCodec.PreparedProtobuf encoded() {
        if (encoded == null) {
            throw new IllegalStateException("Inbound envelope has no prepared encoding");
        }
        return encoded;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void handle(YsmNetContext context) {
        try {
            ((YsmMessageBinding) binding).handler().accept(payload, context);
        } catch (RuntimeException error) {
            YesSteveModel.LOGGER.error("Failed to handle YSM protocol message id={} type={}",
                    binding.spec().id(), binding.spec().messageType().getSimpleName(), error);
            throw error;
        }
    }

    @Override
    public void close() {
        if (encoded != null) {
            encoded.close();
        }
        payload.close();
    }
}
