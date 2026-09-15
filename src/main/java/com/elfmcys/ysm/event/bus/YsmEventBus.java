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

    /**
     * 注册一个事件处理器对象：其所有"公开、单参数、参数类型为 YsmEvent 子类"的方法
     * 都会被注册为对应事件类型的监听器。
     */
    public static void registerHandler(Object handler) {
        for (var method : handler.getClass().getMethods()) {
            if (method.getParameterCount() != 1 || method.isSynthetic()) {
                continue;
            }
            Class<?> param = method.getParameterTypes()[0];
            if (!YsmEvent.class.isAssignableFrom(param)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Class<YsmEvent> eventType = (Class<YsmEvent>) param;
            register(eventType, event -> {
                try {
                    method.invoke(handler, event);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to invoke event handler method " + method, e);
                }
            });
        }
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
