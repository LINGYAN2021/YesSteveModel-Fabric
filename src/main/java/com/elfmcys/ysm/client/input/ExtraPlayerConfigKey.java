package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.gui.ExtraPlayerConfigScreen;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ExtraPlayerConfigKey {
    public static final KeyMapping EXTRA_PLAYER_RENDER_KEY = new KeyMapping("key.yes_steve_model.open_extra_player_render.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            YsmKeyCategories.YSM);

    public static void register() {
        YsmInputDispatcher.registerKeyHandler(ExtraPlayerConfigKey::onKeyboardInput);
    }

    public static void onKeyboardInput(YsmKeyEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS && InputCheckUtil.keyIsMatch(event, EXTRA_PLAYER_RENDER_KEY, true)) {
            YsmInputDispatcher.openScreen(new ExtraPlayerConfigScreen());
        }
    }
}
