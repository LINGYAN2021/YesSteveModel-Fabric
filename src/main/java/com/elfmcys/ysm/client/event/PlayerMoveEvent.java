package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.input.YsmInputDispatcher;
import com.elfmcys.ysm.client.input.YsmKeyEvent;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.fabric.ClientProtocolGateway;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

import static com.elfmcys.ysm.client.input.AnimationRouletteKey.LOCK_ROULETTE_KEY;

public class PlayerMoveEvent {
    private static boolean LOCK_EXTRA_ANIMATION = false;

    public static void register() {
        YsmInputDispatcher.registerKeyHandler(PlayerMoveEvent::onKeyboardInput);
        ClientTickEvents.END_CLIENT_TICK.register(client -> onClientTick());
    }

    public static void onKeyboardInput(YsmKeyEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS && LOCK_ROULETTE_KEY.matches(new net.minecraft.client.input.KeyEvent(event.key(), event.scanCode(), event.modifiers()))) {
            LOCK_EXTRA_ANIMATION = !LOCK_EXTRA_ANIMATION;
        }
    }

    /**
     * 改用客户端 Tick 监听按键状态，避免与其他模组（如 Touch Controller） 冲突时漏判按键。
     */
    public static void onClientTick() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (LOCK_EXTRA_ANIMATION) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && isMoveKey(player)) {
            EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                if (cap.isPlayingExtraAnimation()) {
                    cap.stopExtraAnimation();
                    if (NetworkHandler.isRemoteChannelPresent()) {
                        ClientProtocolGateway.stopSelfAnimation();
                    }
                }
            });
        }
    }

    public static boolean isMoveKey(LocalPlayer player) {
        ClientInput input = player.input;
        if (input == null) {
            return false;
        }
        var moveVector = input.getMoveVector();
        return hasImpulse(moveVector.x) || hasImpulse(moveVector.y)
                || input.keyPresses.jump() || input.keyPresses.shift();
    }

    private static boolean hasImpulse(float impulse) {
        return Math.abs(impulse) > 1.0E-5F;
    }

    public static void switchLock() {
        LOCK_EXTRA_ANIMATION = !LOCK_EXTRA_ANIMATION;
    }

    public static boolean isLocked() {
        return LOCK_EXTRA_ANIMATION;
    }
}
