package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.elfmcys.ysm.network.fabric.ClientProtocolGateway;
import com.elfmcys.ysm.network.fabric.PlayerStateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EntityLoadEvent {
    private static final Cache<Integer, List<Consumer<Entity>>> CACHE = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

    public static void register() {
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> onEntityLoadToWorld(entity));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> onEntityLeaveWorld(entity));
    }

    public static void onEntityLoadToWorld(final Entity entity) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (entity instanceof Player player) {
            ClientProtocolGateway.observePlayer(player.getId(), player.getUUID());
        }
        var list = CACHE.getIfPresent(entity.getId());
        if (list != null) {
            for (var consumer : list) {
                consumer.accept(entity);
            }
        }
        CACHE.invalidate(entity.getId());
    }

    public static void onEntityLeaveWorld(final Entity entity) {
        ClientProtocolGateway.removeEntity(entity.getId());
        PlayerStateHandler.removeClientEntity(entity.getId());
        CACHE.invalidate(entity.getId());
    }

    public static void executeOnEntity(int entityId, Consumer<Entity> consumer) {
        Minecraft.getInstance().execute(() -> {
            var level = Minecraft.getInstance().level;
            if (level != null) {
                var entity = level.getEntity(entityId);
                if (entity != null) {
                    consumer.accept(entity);
                } else {
                    addRecoveryHandler(entityId, consumer);
                }
            }
        });
    }

    // 非线程安全
    private static void addRecoveryHandler(int entityId, Consumer<Entity> consumer) {
        var list = CACHE.getIfPresent(entityId);
        if (list == null) {
            list = new ArrayList<>(3);
            CACHE.put(entityId, list);
        }
        list.add(consumer);
    }
}
