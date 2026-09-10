package com.elfmcys.ysm.config.spec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fabric 侧轻量配置框架，API 形态对齐 ForgeConfigSpec 以最小化调用点改动。
 * 存储为 JSON 文件（config 目录下），注释仅保留在代码中。
 */
public final class YsmConfigSpec {
    private static final Logger LOGGER = LogManager.getLogger("ysm/config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, Entry> entries;

    private YsmConfigSpec(Map<String, Entry> entries) {
        this.entries = entries;
    }

    public void load(Path file) {
        JsonObject root = new JsonObject();
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonElement parsed = GSON.fromJson(reader, JsonElement.class);
                if (parsed != null && parsed.isJsonObject()) {
                    root = parsed.getAsJsonObject();
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to read config {}, using defaults", file, e);
            }
        }
        for (Map.Entry<String, Entry> e : entries.entrySet()) {
            e.getValue().read(root, e.getKey());
        }
        JsonObject out = new JsonObject();
        for (Map.Entry<String, Entry> e : entries.entrySet()) {
            e.getValue().write(out, e.getKey());
        }
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(out, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to write config {}", file, e);
        }
    }

    private static final class Entry {
        final ConfigValue<?> value;

        Entry(ConfigValue<?> value) {
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        void read(JsonObject root, String dottedPath) {
            JsonElement leaf = lookup(root, dottedPath);
            ((ConfigValue<Object>) value).set(parse(leaf, value));
        }

        void write(JsonObject root, String dottedPath) {
            String[] parts = dottedPath.split("\\.");
            JsonObject node = root;
            for (int i = 0; i < parts.length - 1; i++) {
                if (!node.has(parts[i]) || !node.get(parts[i]).isJsonObject()) {
                    node.add(parts[i], new JsonObject());
                }
                node = node.getAsJsonObject(parts[i]);
            }
            node.add(parts[parts.length - 1], value.toJson());
        }

        private static JsonElement lookup(JsonObject root, String dottedPath) {
            JsonObject node = root;
            String[] parts = dottedPath.split("\\.");
            for (int i = 0; i < parts.length - 1; i++) {
                if (!node.has(parts[i]) || !node.get(parts[i]).isJsonObject()) {
                    return null;
                }
                node = node.getAsJsonObject(parts[i]);
            }
            return node.get(parts[parts.length - 1]);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Object parse(JsonElement leaf, ConfigValue<?> value) {
            Object def = value.getDefault();
            try {
                if (def instanceof Boolean) {
                    return leaf != null && leaf.isJsonPrimitive() ? leaf.getAsBoolean() : def;
                }
                if (def instanceof Integer) {
                    return leaf != null && leaf.isJsonPrimitive() ? leaf.getAsInt() : def;
                }
                if (def instanceof Double) {
                    return leaf != null && leaf.isJsonPrimitive() ? leaf.getAsDouble() : def;
                }
                if (def instanceof String) {
                    return leaf != null && leaf.isJsonPrimitive() ? leaf.getAsString() : def;
                }
                if (def instanceof Enum) {
                    if (leaf == null || !leaf.isJsonPrimitive()) {
                        return def;
                    }
                    String name = leaf.getAsString();
                    for (Object constant : def.getClass().getEnumConstants()) {
                        if (((Enum) constant).name().equalsIgnoreCase(name)) {
                            return constant;
                        }
                    }
                    return def;
                }
                if (def instanceof List) {
                    List<String> out = new ArrayList<>();
                    if (leaf != null && leaf.isJsonArray()) {
                        for (JsonElement item : leaf.getAsJsonArray()) {
                            out.add(item.getAsString());
                        }
                    }
                    return out;
                }
            } catch (Exception e) {
                LOGGER.warn("Bad config value, falling back to default {}", def, e);
            }
            return def;
        }
    }

    public static class ConfigValue<T> {
        private final T defaultValue;
        private volatile T value;

        ConfigValue(T defaultValue) {
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public T get() {
            return value;
        }

        T getDefault() {
            return defaultValue;
        }

        void set(T value) {
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        JsonElement toJson() {
            Object v = get();
            if (v instanceof Boolean b) {
                return GSON.toJsonTree(b);
            }
            if (v instanceof Number n) {
                return GSON.toJsonTree(n);
            }
            if (v instanceof Enum<?> e) {
                return GSON.toJsonTree(e.name());
            }
            if (v instanceof List<?> list) {
                JsonArray array = new JsonArray();
                for (Object o : list) {
                    array.add(String.valueOf(o));
                }
                return array;
            }
            return GSON.toJsonTree(String.valueOf(v));
        }
    }

    public static final class BooleanValue extends ConfigValue<Boolean> {
        BooleanValue(boolean defaultValue) {
            super(defaultValue);
        }
    }

    public static final class IntValue extends ConfigValue<Integer> {
        IntValue(int defaultValue) {
            super(defaultValue);
        }
    }

    public static final class DoubleValue extends ConfigValue<Double> {
        DoubleValue(double defaultValue) {
            super(defaultValue);
        }
    }

    public static final class EnumValue<E extends Enum<E>> extends ConfigValue<E> {
        EnumValue(E defaultValue) {
            super(defaultValue);
        }
    }

    public static final class Builder {
        private final Deque<String> path = new ArrayDeque<>();
        private final Map<String, Entry> entries = new LinkedHashMap<>();

        public Builder push(String section) {
            path.addLast(section);
            return this;
        }

        public Builder pop() {
            path.removeLast();
            return this;
        }

        public Builder comment(String comment) {
            return this;
        }

        public BooleanValue define(String key, boolean defaultValue) {
            BooleanValue value = new BooleanValue(defaultValue);
            register(key, value);
            return value;
        }

        public ConfigValue<String> define(String key, String defaultValue) {
            ConfigValue<String> value = new ConfigValue<>(defaultValue);
            register(key, value);
            return value;
        }

        public ConfigValue<List<String>> define(String key, List<String> defaultValue) {
            ConfigValue<List<String>> value = new ConfigValue<>(defaultValue);
            register(key, value);
            return value;
        }

        public IntValue defineInRange(String key, int defaultValue, int min, int max) {
            IntValue value = new IntValue(Math.max(min, Math.min(max, defaultValue)));
            register(key, value);
            return value;
        }

        public DoubleValue defineInRange(String key, double defaultValue, double min, double max) {
            DoubleValue value = new DoubleValue(Math.max(min, Math.min(max, defaultValue)));
            register(key, value);
            return value;
        }

        public <E extends Enum<E>> EnumValue<E> defineEnum(String key, E defaultValue) {
            EnumValue<E> value = new EnumValue<>(defaultValue);
            register(key, value);
            return value;
        }

        public YsmConfigSpec build() {
            return new YsmConfigSpec(entries);
        }

        private void register(String key, ConfigValue<?> value) {
            String full = path.isEmpty() ? key : String.join(".", path) + "." + key;
            entries.put(full, new Entry(value));
        }
    }
}
