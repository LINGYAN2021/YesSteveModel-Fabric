package com.elfmcys.ysm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YesSteveModel implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "ysm";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Yes Steve Model (Fabric 26.1.2 port) initializing");
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Yes Steve Model (Fabric 26.1.2 port) client initializing");
    }
}
