package com.elfmcys.ysm.capability.fabric;

import com.elfmcys.ysm.YesSteveModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fabric 侧 capability 令牌，替代 Forge 的 Capability&lt;T&gt;。
 * 实例按需懒创建并缓存在实体的 mixin 存储中（见 EntityCapabilityHolder）。
 * 注册了读写器的令牌会随实体 NBT 一起持久化。
 */
public final class YsmCapability<T> {
    private static final List<YsmCapability<?>> SERIALIZABLE = new ArrayList<>();

    private final Identifier id;
    private final Factory<T> factory;
    private final @Nullable NbtWriter<T> writer;
    private final @Nullable NbtReader<T> reader;

    private YsmCapability(String path, Factory<T> factory, @Nullable NbtWriter<T> writer, @Nullable NbtReader<T> reader) {
        this.id = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, path);
        this.factory = factory;
        this.writer = writer;
        this.reader = reader;
    }

    public static <T> YsmCapability<T> register(String path, Factory<T> factory) {
        return new YsmCapability<>(path, factory, null, null);
    }

    public static <T> YsmCapability<T> register(String path, Factory<T> factory, NbtWriter<T> writer, NbtReader<T> reader) {
        YsmCapability<T> capability = new YsmCapability<>(path, factory, writer, reader);
        SERIALIZABLE.add(capability);
        return capability;
    }

    public @Nullable T create(Entity entity) {
        return factory.create(entity);
    }

    public String saveKey() {
        return id.getNamespace() + ":" + id.getPath();
    }

    /**
     * 只写出已经存在的实例，不为存档而强制创建
     */
    public static void saveAll(Entity entity, ValueOutput output) {
        Map<YsmCapability<?>, Object> map = EntityCapabilityHolder.mapOf(entity);
        for (YsmCapability<?> capability : SERIALIZABLE) {
            Object instance = map.get(capability);
            if (instance != null) {
                saveOne(capability, instance, output);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void saveOne(YsmCapability<T> capability, Object instance, ValueOutput output) {
        Tag tag = capability.writer.write((T) instance);
        if (tag == null) {
            return;
        }
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("data", tag);
        output.store(capability.saveKey(), CompoundTag.CODEC, wrapper);
    }

    public static void loadAll(Entity entity, ValueInput input) {
        for (YsmCapability<?> capability : SERIALIZABLE) {
            loadOne(capability, entity, input);
        }
    }

    private static <T> void loadOne(YsmCapability<T> capability, Entity entity, ValueInput input) {
        input.read(capability.saveKey(), CompoundTag.CODEC)
                .map(wrapper -> wrapper.get("data"))
                .ifPresent(tag -> EntityCapabilityHolder.get(entity, capability)
                        .ifPresent(instance -> capability.reader.read(instance, tag)));
    }

    @FunctionalInterface
    public interface Factory<T> {
        @Nullable T create(Entity entity);
    }

    @FunctionalInterface
    public interface NbtWriter<T> {
        @Nullable Tag write(T instance);
    }

    @FunctionalInterface
    public interface NbtReader<T> {
        void read(T instance, Tag tag);
    }
}
