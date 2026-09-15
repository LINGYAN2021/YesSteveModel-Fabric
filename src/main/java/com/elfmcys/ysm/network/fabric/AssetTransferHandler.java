package com.elfmcys.ysm.network.fabric;

import com.elfmcys.ysm.client.model.ClientModelService;
import com.elfmcys.ysm.model.server.ServerModelService;
import com.elfmcys.ysm.network.NetworkPayload;
import com.elfmcys.ysm.network.protocol.ProtocolUuid;
import com.elfmcys.ysm.proto.network.protocol.v0.AssetTransferV0;


public final class AssetTransferHandler {
    private AssetTransferHandler() {
    }

    public static void handleFragmentPayload(NetworkPayload<AssetTransferV0.AssetFragment> payload,
                                             YsmNetContext context) {
        if (context.sender() == null) {
            try {
                context.enqueueWork(() -> {
                    try (payload) {
                        ClientModelService.current().ifPresent(service -> service.receive(payload));
                    }
                });
            } catch (Throwable error) {
                payload.close();
                throw error;
            }
        } else {
            payload.close();
        }
    }

    public static void handleBatchRequest(AssetTransferV0.ModelAssetBatchRequest request,
                                          YsmNetContext context) {
        var sender = context.sender();
        if (sender != null) {
            context.enqueueWork(() -> ServerModelService.current()
                    .ifPresent(service -> service.handleRequest(sender, request)));
        }
    }

    public static void handleBatchFailure(AssetTransferV0.ModelAssetBatchFailure failure,
                                          YsmNetContext context) {
        if (context.sender() == null) {
            context.enqueueWork(() -> ClientModelService.current()
                    .ifPresent(service -> service.requestFailed(failure)));
        }
    }

    public static void handleCatalogResync(AssetTransferV0.CatalogResyncRequest request,
                                           YsmNetContext context) {
        var sender = context.sender();
        if (sender != null) {
            context.enqueueWork(() -> ServerModelService.current()
                    .ifPresent(service -> service.sendCatalog(sender)));
        }
    }

    public static void handleBatchCancel(AssetTransferV0.ModelAssetBatchCancel cancel,
                                         YsmNetContext context) {
        var sender = context.sender();
        if (sender != null) {
            try {
                var requestId = ProtocolUuid.get(cancel.getRequestId());
                context.enqueueWork(() -> ServerModelService.current()
                        .ifPresent(service -> service.cancelSession(sender.getUUID(), requestId)));
            } catch (RuntimeException ignored) {
            }
        }
    }

    public static void handleTransferRelease(AssetTransferV0.AssetTransferRelease release,
                                             YsmNetContext context) {
        var sender = context.sender();
        if (sender != null) {
            context.enqueueWork(() -> ServerModelService.current()
                    .ifPresent(service -> service.releaseTransfer(sender.getUUID(), release)));
        }
    }
}
