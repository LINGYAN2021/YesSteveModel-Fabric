package com.elfmcys.ysm.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.AuthModelsCapability;
import com.elfmcys.ysm.capability.AuthModelsCapabilityProvider;
import com.elfmcys.ysm.capability.ModelInfoCapability;
import com.elfmcys.ysm.capability.ModelInfoCapabilityProvider;
import com.elfmcys.ysm.capability.ModelInfoSyncAssembler;
import com.elfmcys.ysm.capability.ProjectileModelInfoCapabilityProvider;
import com.elfmcys.ysm.capability.StarModelsCapability;
import com.elfmcys.ysm.capability.StarModelsCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleModelInfoCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.model.server.ServerModelService;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.fabric.ControlHandler;
import com.elfmcys.ysm.network.fabric.HandshakeHandler;
import com.elfmcys.ysm.network.fabric.MinecraftStateHandler;
import com.elfmcys.ysm.proto.network.protocol.v0.PlayerStateV0;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.Optional;

public final class CapabilityEvent {
    private CapabilityEvent() {
    }

    public static void register() {
        ServerPlayerEvents.COPY_FROM.register(CapabilityEvent::onPlayerCloned);
        EntityTrackingEvents.START_TRACKING.register(CapabilityEvent::onTrackingPlayer);
        ServerEntityEvents.ENTITY_LOAD.register(CapabilityEvent::onEntityJoinWorld);
        ServerTickEvents.END_SERVER_TICK.register(CapabilityEvent::onServerTick);
    }

    private static void onPlayerCloned(ServerPlayer original, ServerPlayer clone, boolean alive) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        ModelInfoCapability oldModelInfo = EntityCapabilityHolder.peek(original, ModelInfoCapabilityProvider.MODEL_INFO_CAP);
        AuthModelsCapability oldAuthModels = EntityCapabilityHolder.peek(original, AuthModelsCapabilityProvider.AUTH_MODELS_CAP);
        StarModelsCapability oldStarModels = EntityCapabilityHolder.peek(original, StarModelsCapabilityProvider.STAR_MODELS_CAP);

        if (oldModelInfo != null) {
            clone.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP)
                    .ifPresent(newModelInfo -> newModelInfo.moveFrom(oldModelInfo));
        }
        if (oldAuthModels != null) {
            clone.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP)
                    .ifPresent(newAuthModels -> newAuthModels.copyFrom(oldAuthModels));
        }
        if (oldStarModels != null) {
            clone.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP)
                    .ifPresent(newStarModels -> newStarModels.copyFrom(oldStarModels));
        }
    }

    private static void onTrackingPlayer(Entity tracked, ServerPlayer tracker) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (tracked instanceof ServerPlayer trackPlayer) {
            getModelInfoCap(trackPlayer).ifPresent(cap -> {
                if (!NetworkHandler.isPlayerChannelPresent(trackPlayer) && !cap.isMandatory()) {
                    return;
                }
                buildModelInfoPacket(trackPlayer, cap).ifPresentOrElse(packet ->
                        NetworkHandler.sendToClientPlayer(packet, tracker), cap::markDirty);
            });
        } else if (tracked instanceof Projectile projectile) {
            projectile.getCapability(ProjectileModelInfoCapabilityProvider.CAP).ifPresent(cap -> {
                if (cap.isInitialized()) {
                    NetworkHandler.sendToClientPlayer(
                            MinecraftStateHandler.projectile(projectile.getId(), cap), tracker);
                }
            });
        } else {
            tracked.getCapability(VehicleModelInfoCapabilityProvider.CAP).ifPresent(cap -> {
                if (cap.isInitialized()) {
                    NetworkHandler.sendToClientPlayer(
                            MinecraftStateHandler.vehicle(tracked.getId(), cap), tracker);
                }
            });
        }
    }

    private static void onEntityJoinWorld(Entity entity, ServerLevel level) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (entity instanceof ServerPlayer serverPlayer) {
            getModelInfoCap(serverPlayer).ifPresent(modelInfoCap -> {
                if (!NetworkHandler.isPlayerChannelPresent(serverPlayer) && !modelInfoCap.isMandatory()) {
                    modelInfoCap.markDirty();
                    return;
                }
                modelInfoCap.stopAnimation(serverPlayer);
                buildModelInfoPacket(serverPlayer, modelInfoCap).ifPresentOrElse(packet ->
                        NetworkHandler.sendToClientPlayer(packet, serverPlayer), modelInfoCap::markDirty);
            });

            getAuthModelsCap(serverPlayer).ifPresent(authModelsCap ->
                    NetworkHandler.sendToClientPlayer(
                            ControlHandler.authorizedModels(authModelsCap.getAuthModels(), 1), serverPlayer));

            getStarModelsCap(serverPlayer).ifPresent(starModelCap ->
                    NetworkHandler.sendToClientPlayer(
                            ControlHandler.starredModels(starModelCap.getStarModels(), 1), serverPlayer));
        }
    }

    /**
     * 同步客户端服务端数据
     */
    private static void onServerTick(MinecraftServer server) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        var players = server.getPlayerList().getPlayers();
        var lowBandwidthUsage = ServerConfig.LOW_BANDWIDTH_USAGE.get();
        for (ServerPlayer player : players) {
            getModelInfoCap(player).ifPresent(cap -> {
                if (!NetworkHandler.isPlayerChannelPresent(player) && !cap.isMandatory()) {
                    if (player.tickCount == 200 || player.tickCount == 600 || player.tickCount == 1800) {
                        HandshakeHandler.sendServerHello(player);
                    }
                    return;
                }
                if (cap.isDirty()) {
                    cap.getPropertiesTracker().tick(player, false, lowBandwidthUsage);
                    buildModelInfoPacket(player, cap).ifPresent(packet -> {
                        cap.clearDirty();
                        NetworkHandler.broadcastToVisiblePlayersAndSelf(packet, player);
                        if (player.getVehicle() != null && player.getVehicle().getFirstPassenger() == player) {
                            CapabilityEvent.onVehicleSetModel(player.getVehicle(), player);
                        }
                    });
                } else {
                    cap.getPropertiesTracker().tick(player, true, lowBandwidthUsage);
                }
            });
        }
    }

    public static void onProjectileSetOwner(Projectile projectile, ServerPlayer owner) {
        owner.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(ownerCap -> {
            if (!NetworkHandler.isPlayerChannelPresent(owner) && !ownerCap.isMandatory()) {
                return;
            }
            projectile.getCapability(ProjectileModelInfoCapabilityProvider.CAP).ifPresent(cap ->
                    ownerCap.executeWithMolangVars(molangVars -> {
                        cap.init(ownerCap.getModelId(), molangVars);
                        NetworkHandler.broadcastToVisiblePlayers(
                                MinecraftStateHandler.projectile(projectile.getId(), cap), projectile);
                    }));
        });
    }

    public static void onVehicleSetModel(Entity vehicle, ServerPlayer owner) {
        owner.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(ownerCap -> {
            if (!NetworkHandler.isPlayerChannelPresent(owner) && !ownerCap.isMandatory()) {
                return;
            }
            vehicle.getCapability(VehicleModelInfoCapabilityProvider.CAP).ifPresent(cap ->
                    // 失败就丢弃
                    ownerCap.getMolangVars().ifPresent(molangVars -> {
                        cap.update(ownerCap.getModelId(), molangVars);
                        NetworkHandler.broadcastToVisiblePlayers(
                                MinecraftStateHandler.vehicle(vehicle.getId(), cap), vehicle);
                    }));
        });
    }

    private static Optional<ModelInfoCapability> getModelInfoCap(Player player) {
        return player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP);
    }

    private static Optional<PlayerStateV0.PlayerStateUpdate> buildModelInfoPacket(
            ServerPlayer player, ModelInfoCapability capability) {
        return ServerModelService.current().flatMap(ServerModelService::snapshot)
                .flatMap(snapshot -> ModelInfoSyncAssembler.build(player, capability, snapshot));
    }

    private static Optional<AuthModelsCapability> getAuthModelsCap(Player player) {
        return player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP);
    }

    private static Optional<StarModelsCapability> getStarModelsCap(Player player) {
        return player.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP);
    }
}
