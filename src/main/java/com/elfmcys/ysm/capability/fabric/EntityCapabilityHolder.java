package com.elfmcys.ysm.capability.fabric;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * 通过 fabric.mod.json 的 fabric:injected_interfaces 注入到 Entity 上，
 * 让既有调用点 EntityCapabilityHolder.get(entity, CAP) 保持不变。
 */
public interface EntityCapabilityHolder {
    @SuppressWarnings("unchecked")
    default <T> Optional<T> getCapability(YsmCapability<T> capability) {
        Map<YsmCapability<?>, Object> map = mapOf((Entity) this);
        Object instance = map.get(capability);
        if (instance == null) {
            instance = capability.create((Entity) this);
            if (instance == null) {
                return Optional.empty();
            }
            map.put(capability, instance);
        }
        return Optional.of((T) instance);
    }

    static Map<YsmCapability<?>, Object> mapOf(Entity entity) {
        return ((EntityCapabilityStore) entity).ysm$capabilityMap();
    }

    static <T> Optional<T> get(Entity entity, YsmCapability<T> capability) {
        return ((EntityCapabilityHolder) entity).getCapability(capability);
    }

    static <T> T put(Entity entity, YsmCapability<T> capability, T instance) {
        mapOf(entity).put(capability, instance);
        return instance;
    }

    static <T> @Nullable T peek(Entity entity, YsmCapability<T> capability) {
        @SuppressWarnings("unchecked")
        T instance = (T) mapOf(entity).get(capability);
        return instance;
    }
}
