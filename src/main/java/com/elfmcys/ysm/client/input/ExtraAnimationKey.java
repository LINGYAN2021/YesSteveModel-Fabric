package com.elfmcys.ysm.client.input;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.event.PlayerMoveEvent;
import com.elfmcys.ysm.client.gui.AnimationRouletteScreen;
import com.elfmcys.ysm.info.ModelProperties;
import com.elfmcys.ysm.network.fabric.ClientProtocolGateway;
import com.elfmcys.ysm.util.InputCheckUtil;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static com.elfmcys.ysm.client.gui.AnimationRouletteScreen.addRootClassify;

public class ExtraAnimationKey {
    public static final List<KeyMapping> EXTRA_ANIMATION_KEYS = Lists.newArrayList();

    public static void register() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        for (int i = 0; i <= 7; i++) {
            String name = String.format("key.yes_steve_model.extra_animation.%d.desc", i);
            KeyMapping keyMapping = new KeyMapping(name,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN,
                    YsmKeyCategories.YSM);
            KeyMappingHelper.registerKeyMapping(keyMapping);
            EXTRA_ANIMATION_KEYS.add(keyMapping);
        }
        YsmInputDispatcher.registerKeyHandler(ExtraAnimationKey::onKeyboardInput);
    }

    public static void onKeyboardInput(YsmKeyEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        for (KeyMapping key : EXTRA_ANIMATION_KEYS) {
            if (event.getAction() == GLFW.GLFW_PRESS && InputCheckUtil.keyIsMatch(event, key)
                && player != null && !PlayerMoveEvent.isMoveKey(player)) {
                EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                    var model = cap.getModelRenderTarget();
                    int index = EXTRA_ANIMATION_KEYS.indexOf(key);
                    ModelProperties properties = model.info().properties();
                    var animationMap = properties.extraAnimationOrderMap();
                    if (animationMap.size() > index) {
                        String keyName = animationMap.getKeyAt(index);
                        if ("#return".equals(keyName)) {
                            // #return 为停止播放轮盘动画
                            ClientProtocolGateway.stopSelfAnimation();
                        } else if (keyName.startsWith("#") && properties.extraAnimationClassifyMap().containsKey(keyName.substring(1))) {
                            addRootClassify(keyName.substring(1));
                            AnimationRouletteScreen screen = new AnimationRouletteScreen(
                                    properties.extraAnimationButtonsMap(),
                                    properties.extraAnimationClassifyMap(),
                                    model, cap
                            );
                            YsmInputDispatcher.openScreen(screen);
                        } else {
                            // 本地先播，避免等服务端往返
                            cap.playExtraAnimation(keyName);
                            ClientProtocolGateway.playSelfAnimation(keyName);
                        }
                    }
                });
                return;
            }
        }
    }
}
