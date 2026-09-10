package com.elfmcys.ysm.network.fabric;

import com.elfmcys.ysm.network.NetworkPayload;

public final class ServerboundEnvelope extends YsmEnvelope {
    public ServerboundEnvelope(YsmMessageBinding<?> binding, NetworkPayload<?> payload, boolean outbound) {
        super(binding, payload, outbound);
    }
}
