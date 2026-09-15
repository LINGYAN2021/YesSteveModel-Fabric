package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.compat.touhoulittlemaid.client.TlmClientCompat;
import com.elfmcys.ysm.client.gui.AnimationRouletteScreen;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AnimationRouletteKey {
    public static final KeyMapping ANIMATION_ROULETTE_KEY = new KeyMapping("key.yes_steve_model.animation_roulette.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            YsmKeyCategories.YSM);

    public static final KeyMapping LOCK_ROULETTE_KEY = new KeyMapping("key.yes_steve_model.lock_roulette.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            YsmKeyCategories.YSM);

    public static void register() {
        YsmInputDispatcher.registerKeyHandler(AnimationRouletteKey::onKeyboardInput);
    }

    public static void onKeyboardInput(YsmKeyEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS && InputCheckUtil.keyIsMatch(event, ANIMATION_ROULETTE_KEY)
            && (!NetworkHandler.isRemoteChannelPresent() || ServerConfig.CAN_SWITCH_MODEL.get())) {
            if (TlmClientCompat.pointToMaid()) {
                TlmClientCompat.onRouletteMainKeyPressed();
            } else if (Minecraft.getInstance().player != null) {
                EntityCapabilityHolder.get(Minecraft.getInstance().player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                    String modelId = cap.getModelId();
                    var model = cap.getModelRenderTarget();
                    if (model != null && !model.info().properties().extraAnimationOrderMap().isEmpty()) {
                        if (Minecraft.getInstance().screen == null) {
                            YsmInputDispatcher.openScreen(new AnimationRouletteScreen(modelId, model, cap));
                            return;
                        }
                        if (Minecraft.getInstance().screen instanceof AnimationRouletteScreen) {
                            YsmInputDispatcher.openScreen(null);
                        }
                    }
                });
            }
        }
    }
}
