package com.elfmcys.ysm.network.fabric;

import com.elfmcys.ysm.network.NetworkPayload;

public final class ClientboundEnvelope extends YsmEnvelope {
    public ClientboundEnvelope(YsmMessageBinding<?> binding, NetworkPayload<?> payload, boolean outbound) {
        super(binding, payload, outbound);
    }
}
