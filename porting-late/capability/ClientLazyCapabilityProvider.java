package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.capability.fabric.YsmCapability;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class ClientLazyCapabilityProvider {
    public static final YsmCapability<ClientLazyCapability> CAP = YsmCapability.register(
            "client_lazy",
            entity -> FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && entity.level().isClientSide()
                    ? new ClientLazyCapability(entity) : null);

    private ClientLazyCapabilityProvider() {
    }
}
