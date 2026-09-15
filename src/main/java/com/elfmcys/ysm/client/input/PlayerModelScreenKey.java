package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.gui.ConfigScreen;
import com.elfmcys.ysm.client.gui.DisclaimerScreen;
import com.elfmcys.ysm.client.gui.PlayerModelScreen;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class PlayerModelScreenKey {
    public static final KeyMapping PLAYER_MODEL_KEY = new KeyMapping("key.yes_steve_model.player_model.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            YsmKeyCategories.YSM);

    public static void register() {
        YsmInputDispatcher.registerKeyHandler(PlayerModelScreenKey::onKeyboardInput);
    }

    public static void onKeyboardInput(YsmKeyEvent event) {
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS && InputCheckUtil.keyIsMatch(event, PLAYER_MODEL_KEY, true)) {
            if (!YesSteveModel.isAvailable()) {
                YesSteveModel.sendUnavailableMessage();
                return;
            }
            if (NetworkHandler.isRemoteChannelPresent() && !ServerConfig.CAN_SWITCH_MODEL.get()) {
                YsmInputDispatcher.openScreen(new ConfigScreen(null));
                return;
            }
            if (ClientConfig.DISCLAIMER_SHOW.get()) {
                YsmInputDispatcher.openScreen(new DisclaimerScreen());
            } else {
                YsmInputDispatcher.openScreen(new PlayerModelScreen());
            }
        }
    }
}
