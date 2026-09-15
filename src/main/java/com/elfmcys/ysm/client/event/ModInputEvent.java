package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.input.YsmInputDispatcher;
import com.elfmcys.ysm.client.input.YsmKeyEvent;
import com.elfmcys.ysm.client.input.YsmMouseEvent;
import com.elfmcys.ysm.util.InputCheckUtil;
import org.lwjgl.glfw.GLFW;

public class ModInputEvent {
    public static volatile boolean[] KEY_STATES = new boolean[GLFW.GLFW_KEY_LAST + 1];
    public static volatile boolean[] MOUSE_STATES = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];

    public static void register() {
        YsmInputDispatcher.registerKeyHandler(ModInputEvent::onKeyInput);
        YsmInputDispatcher.registerMouseHandler(ModInputEvent::onMouseInput);
    }

    public static void onKeyInput(YsmKeyEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (GLFW.GLFW_KEY_SPACE <= event.key() && event.key() <= GLFW.GLFW_KEY_LAST) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                KEY_STATES[event.key()] = true;
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                KEY_STATES[event.key()] = false;
            }
        }
    }

    public static void onMouseInput(YsmMouseEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (!InputCheckUtil.isInGame()) {
            return;
        }
        if (GLFW.GLFW_MOUSE_BUTTON_1 <= event.button() && event.button() <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                MOUSE_STATES[event.button()] = true;
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                MOUSE_STATES[event.button()] = false;
            }
        }
    }
}
