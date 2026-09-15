package com.elfmcys.ysm.util;

import com.elfmcys.ysm.client.input.YsmKeyEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class InputCheckUtil {
    private static final int MOD_MASK = GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL
            | GLFW.GLFW_MOD_ALT | GLFW.GLFW_MOD_SUPER;

    /**
     * 仅匹配键位，不检查修饰键。
     */
    public static boolean keyIsMatch(YsmKeyEvent event, KeyMapping keyMapping) {
        return keyMapping.matches(new KeyEvent(event.key(), event.scanCode(), event.modifiers()));
    }

    /**
     * 匹配键位，且要求 Alt 修饰键状态与期望一致（Forge KeyModifier 的替代品）。
     */
    public static boolean keyIsMatch(YsmKeyEvent event, KeyMapping keyMapping, boolean needAlt) {
        if (!keyMapping.matches(new KeyEvent(event.key(), event.scanCode(), event.modifiers()))) {
            return false;
        }
        boolean altDown = (event.modifiers() & MOD_MASK) == GLFW.GLFW_MOD_ALT;
        return altDown == needAlt;
    }

    public static boolean isInGame() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (mc.getOverlay() != null) {
            return false;
        }
        // 不能打开任何 GUI
        if (mc.screen != null) {
            return false;
        }
        // 当前窗口捕获鼠标操作
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        // 选择了当前窗口
        return mc.isWindowActive();
    }
}
