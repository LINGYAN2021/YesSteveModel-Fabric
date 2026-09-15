package com.elfmcys.ysm;

import com.elfmcys.ysm.client.event.ClientLoggedEvent;
import com.elfmcys.ysm.client.event.ClientSetupEvent;
import com.elfmcys.ysm.client.event.ClientTickEvent;
import com.elfmcys.ysm.client.event.EntityLoadEvent;
import com.elfmcys.ysm.client.event.ModInputEvent;
import com.elfmcys.ysm.client.event.PlayerMoveEvent;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.elfmcys.ysm.client.event.VanillaPlayerRenderEvent;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.event.CapabilityEvent;
import com.elfmcys.ysm.event.CommandRegistry;
import com.elfmcys.ysm.event.LoggedOutEvent;
import com.elfmcys.ysm.event.LoginEvent;
import com.elfmcys.ysm.event.ServerStartingEvent;
import com.elfmcys.ysm.event.bus.YsmEvent;
import com.elfmcys.ysm.event.bus.YsmEventBus;
import com.elfmcys.ysm.init.ModSounds;
import com.elfmcys.ysm.model.ModelRuntime;
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
        } else {
            ModelRuntime.initialize();
        }

        NetworkHandler.init();

        ModSounds.init();
        CapabilityEvent.register();
        LoginEvent.register();
        LoggedOutEvent.register();
        ServerStartingEvent.register();
        CommandRegistry.register();
    }

    @Override
    public void onInitializeClient() {
        ClientConfig.init().load(FabricLoader.getInstance().getConfigDir().resolve("ysm-client.json"));
        NetworkHandler.initClient();

        ClientLoggedEvent.register();
        ClientTickEvent.register();
        EntityLoadEvent.register();
        PlayerMoveEvent.register();
        ModInputEvent.register();
        RegisterEntityRenderersEvent.register();
        VanillaPlayerRenderEvent.register();

        ClientSetupEvent.registerKeyMappings();
        ClientSetupEvent.registerGuiOverlays();
        ClientSetupEvent.onClientSetup();
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

    public static net.minecraft.network.chat.Component getUnavailableMessage() {
        return NativeLibUtil.getUnsupportedMsg();
    }

    public static String getUnavailableMessageString() {
        return NativeLibUtil.getUnsupportedMsgStr();
    }

    public static void sendUnavailableMessage() {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(getUnavailableMessage());
        }
    }
}
