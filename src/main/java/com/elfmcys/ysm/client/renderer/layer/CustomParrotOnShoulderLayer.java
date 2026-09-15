package com.elfmcys.ysm.client.renderer.layer;

import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CustomParrotOnShoulderLayer extends GeoLayerRenderer<CustomPlayerEntity> {
    private final ParrotModel model;

    public CustomParrotOnShoulderLayer(EntityRendererProvider.Context context) {
        this.model = new ParrotModel(context.bakeLayer(ModelLayers.PARROT));
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, CustomPlayerEntity animatable, GeoRenderData renderData, int packedLight, int overlay) {
        // TODO: 肩膀鹦鹉骨骼定位渲染尚未在新版实现
    }
}
