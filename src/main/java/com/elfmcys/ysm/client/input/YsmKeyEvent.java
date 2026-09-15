package com.elfmcys.ysm.client.input;

/**
 * Fabric 没有 Forge 的 InputEvent.Key，这里用等价的轻量记录承载原始按键事件。
 */
public record YsmKeyEvent(int key, int scanCode, int action, int modifiers) {
    public int getKey() {
        return key;
    }

    public int getScanCode() {
        return scanCode;
    }

    public int getAction() {
        return action;
    }

    public int getModifiers() {
        return modifiers;
    }
}
