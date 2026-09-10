package com.elfmcys.ysm.network.fabric;

import com.elfmcys.ysm.network.NetworkPayload;
import com.elfmcys.ysm.network.protocol.ProtocolMessageSpec;
import us.hebi.quickbuf.ProtoMessage;

import java.util.function.BiConsumer;

public record YsmMessageBinding<T extends ProtoMessage<T>>(
        ProtocolMessageSpec<T> spec,
        YsmProtoCodec.Parser<T> parser,
        BiConsumer<NetworkPayload<T>, YsmNetContext> handler) {
}
