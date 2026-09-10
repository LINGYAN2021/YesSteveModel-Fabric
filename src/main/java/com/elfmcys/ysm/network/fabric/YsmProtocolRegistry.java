package com.elfmcys.ysm.network.fabric;

import com.elfmcys.ysm.network.protocol.MessageDirection;
import us.hebi.quickbuf.ProtoMessage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class YsmProtocolRegistry {
    private static final Map<Integer, YsmMessageBinding<?>> CLIENTBOUND = new ConcurrentHashMap<>();
    private static final Map<Integer, YsmMessageBinding<?>> SERVERBOUND = new ConcurrentHashMap<>();

    private YsmProtocolRegistry() {
    }

    public static synchronized <T extends ProtoMessage<T>> void add(YsmMessageBinding<T> binding) {
        var bindings = binding.spec().direction() == MessageDirection.CLIENT_TO_SERVER
                ? SERVERBOUND : CLIENTBOUND;
        if (bindings.putIfAbsent(binding.spec().id(), binding) != null) {
            throw new IllegalArgumentException("Duplicate Forge protocol binding: " + binding.spec().id());
        }
    }

    public static Optional<YsmMessageBinding<?>> find(int id, MessageDirection direction) {
        return Optional.ofNullable((direction == MessageDirection.CLIENT_TO_SERVER
                ? SERVERBOUND : CLIENTBOUND).get(id));
    }
}
