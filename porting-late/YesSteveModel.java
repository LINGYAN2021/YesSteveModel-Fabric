package com.elfmcys.ysm;

import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.event.bus.YsmEvent;
import com.elfmcys.ysm.event.bus.YsmEventBus;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.util.NativeLibUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class YesSteveModel implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "ysm";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerConfig.init().load(FabricLoader.getInstance().getConfigDir().resolve("ysm-server.json"));

        try {
            NativeLibUtil.load();
        } catch (IOException e) {
            LOGGER.error("Failed to load YSM native library", e);
        }
        if (!NativeLibUtil.isAvailable()) {
            LOGGER.error("YSM native library unavailable, mod features are disabled");
        }

        NetworkHandler.init();
    }

    @Override
    public void onInitializeClient() {
        ClientConfig.init().load(FabricLoader.getInstance().getConfigDir().resolve("ysm-client.json"));
        NetworkHandler.initClient();
    }

    public static boolean postEvent(YsmEvent event) {
        YsmEventBus.post(event);
        return event.isCanceled();
    }

    public static boolean isAvailable() {
        return NativeLibUtil.isAvailable();
    }

    public static boolean isMobilePlatform() {
        return NativeLibUtil.isMobilePlatform();
    }

    public static String getUnavailableWarningKey() {
        return NativeLibUtil.getUnavailableWarningKey();
    }

    public static Object[] getUnavailableWarningArgs() {
        return NativeLibUtil.getUnavailableWarningArgs();
    }
}
