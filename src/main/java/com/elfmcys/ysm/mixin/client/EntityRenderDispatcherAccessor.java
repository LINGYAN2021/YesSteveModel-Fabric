package com.elfmcys.ysm.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(EntityRenderDispatcher.class)
public interface EntityRenderDispatcherAccessor {
    @Accessor("blockModelResolver")
    BlockModelResolver ysm$blockModelResolver();

    @Accessor("itemModelResolver")
    ItemModelResolver ysm$itemModelResolver();

    @Accessor("mapRenderer")
    MapRenderer ysm$mapRenderer();

    @Accessor("atlasManager")
    AtlasManager ysm$atlasManager();

    @Accessor("font")
    Font ysm$font();

    @Accessor("entityModels")
    Supplier<EntityModelSet> ysm$entityModels();

    @Accessor("equipmentAssets")
    EquipmentAssetManager ysm$equipmentAssets();

    @Accessor("playerSkinRenderCache")
    PlayerSkinRenderCache ysm$playerSkinRenderCache();
}
