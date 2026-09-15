package com.elfmcys.ysm.client.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 由 KeyboardHandlerMixin / MouseHandlerMixin 触发的原始输入事件分发器，
 * 用来替代 Forge 的 InputEvent.Key / InputEvent.MouseButton 总线事件。
 */
public final class YsmInputDispatcher {
    public interface KeyHandler {
        void onKey(YsmKeyEvent event);
    }

    public interface MouseHandler {
        void onMouse(YsmMouseEvent event);
    }

    private static final List<KeyHandler> KEY_HANDLERS = new CopyOnWriteArrayList<>();
    private static final List<MouseHandler> MOUSE_HANDLERS = new CopyOnWriteArrayList<>();

    private YsmInputDispatcher() {
    }

    public static void registerKeyHandler(KeyHandler handler) {
        KEY_HANDLERS.add(handler);
    }

    public static void registerMouseHandler(MouseHandler handler) {
        MOUSE_HANDLERS.add(handler);
    }

    public static void fireKey(int key, int scanCode, int action, int modifiers) {
        if (KEY_HANDLERS.isEmpty()) {
            return;
        }
        YsmKeyEvent event = new YsmKeyEvent(key, scanCode, action, modifiers);
        for (KeyHandler handler : KEY_HANDLERS) {
            handler.onKey(event);
        }
    }

    public static void fireMouse(int button, int action, int modifiers) {
        if (MOUSE_HANDLERS.isEmpty()) {
            return;
        }
        YsmMouseEvent event = new YsmMouseEvent(button, action, modifiers);
        for (MouseHandler handler : MOUSE_HANDLERS) {
            handler.onMouse(event);
        }
    }

    /**
     * 26.1.2 的 KeyboardHandler.keyPress 在处理过程中才读取 mc.screen，
     * 按键处理器里同步 setScreen 会让刚打开的界面收到同一个按键事件
     * （PlayerModelScreen 会因此立刻把自己关掉，表现为 Alt+Y 只能打开一次），
     * 所以界面开关统一延迟到下一 tick 执行。
     */
    public static void openScreen(@Nullable Screen screen) {
        var minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.setScreen(screen));
    }
}
