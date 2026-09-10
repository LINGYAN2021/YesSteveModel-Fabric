package com.elfmcys.ysm.event.bus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Fabric 侧极简事件总线，替代 MinecraftForge.EVENT_BUS。
 * 仅承载 YSM 自定义事件；原版/MC 生命周期事件走 Fabric API 显式注册。
 */
public final class YsmEventBus {
    private static final Map<Class<?>, List<Consumer<?>>> LISTENERS = new ConcurrentHashMap<>();

    private YsmEventBus() {
    }

    public static <T> void register(Class<T> eventType, Consumer<T> listener) {
        LISTENERS.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T> T post(T event) {
        List<Consumer<?>> listeners = LISTENERS.get(event.getClass());
        if (listeners != null) {
            for (Consumer<?> listener : listeners) {
                ((Consumer<T>) listener).accept(event);
            }
        }
        return event;
    }
}
