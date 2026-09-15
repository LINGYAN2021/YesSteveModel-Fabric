package com.elfmcys.ysm.client.input;

/**
 * Fabric 没有 Forge 的 InputEvent.MouseButton，这里用等价的轻量记录承载原始鼠标事件。
 */
public record YsmMouseEvent(int button, int action, int modifiers) {
    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public int getModifiers() {
        return modifiers;
    }
}
