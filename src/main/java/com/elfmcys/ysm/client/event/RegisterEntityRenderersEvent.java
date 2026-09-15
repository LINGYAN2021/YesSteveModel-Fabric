package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.compat.backpack.sophisticated.SophisticatedCompat;
import com.elfmcys.ysm.client.renderer.CustomFirstPersonArmRenderer;
import com.elfmcys.ysm.client.renderer.CustomPlayerRenderer;
import com.elfmcys.ysm.client.renderer.CustomProjectileRenderer;
import com.elfmcys.ysm.client.renderer.CustomVehicleRenderer;
import com.elfmcys.ysm.client.renderer.YsmRendererContexts;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

public class RegisterEntityRenderersEvent {
    private static CustomPlayerRenderer CUSTOM_PLAYER_RENDERER;
    private static CustomProjectileRenderer CUSTOM_PROJECTILE_RENDERER;
    private static CustomFirstPersonArmRenderer CUSTOM_FIRST_PERSON_RENDERER;
    private static CustomVehicleRenderer CUSTOM_VEHICLE_RENDERER;

    private static void init(ResourceManager resourceManager) {
        var context = YsmRendererContexts.get();

        CUSTOM_PLAYER_RENDERER = new CustomPlayerRenderer(context);
        CUSTOM_PROJECTILE_RENDERER = new CustomProjectileRenderer(context);
        CUSTOM_FIRST_PERSON_RENDERER = new CustomFirstPersonArmRenderer();
        CUSTOM_VEHICLE_RENDERER = new CustomVehicleRenderer(context);

        SophisticatedCompat.addLayer();
    }

    public static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "entity_renderers");
            }

            @Override
            public java.util.concurrent.CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState,
                                                                       java.util.concurrent.Executor backgroundExecutor,
                                                                       PreparableReloadListener.PreparationBarrier preparationBarrier,
                                                                       java.util.concurrent.Executor gameExecutor) {
                return preparationBarrier.wait(net.minecraft.util.Unit.INSTANCE).thenRunAsync(() -> {
                    if (YesSteveModel.isAvailable()) {
                        init(sharedState.resourceManager());
                    }
                }, gameExecutor);
            }
        });
    }

    public static CustomPlayerRenderer getPlayerRenderer() {
        if (CUSTOM_PLAYER_RENDERER == null) {
            init(net.minecraft.client.Minecraft.getInstance().getResourceManager());
        }
        return CUSTOM_PLAYER_RENDERER;
    }

    public static CustomProjectileRenderer getProjectRenderer() {
        if (CUSTOM_PROJECTILE_RENDERER == null) {
            init(net.minecraft.client.Minecraft.getInstance().getResourceManager());
        }
        return CUSTOM_PROJECTILE_RENDERER;
    }

    public static CustomFirstPersonArmRenderer getFirstPersonArmRenderer() {
        if (CUSTOM_FIRST_PERSON_RENDERER == null) {
            init(net.minecraft.client.Minecraft.getInstance().getResourceManager());
        }
        return CUSTOM_FIRST_PERSON_RENDERER;
    }

    public static CustomVehicleRenderer getVehicleRenderer() {
        if (CUSTOM_VEHICLE_RENDERER == null) {
            init(net.minecraft.client.Minecraft.getInstance().getResourceManager());
        }
        return CUSTOM_VEHICLE_RENDERER;
    }
}
