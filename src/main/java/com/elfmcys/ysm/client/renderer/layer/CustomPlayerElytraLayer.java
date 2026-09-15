package com.elfmcys.ysm.client.renderer.layer;

import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.elfmcys.ysm.util.EquipmentUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class CustomPlayerElytraLayer extends GeoLayerRenderer<CustomPlayerEntity> {
    private static final Identifier WINGS_LOCATION = Identifier.parse("textures/entity/elytra.png");
    private final ElytraModel elytraModel;

    public CustomPlayerElytraLayer(EntityRendererProvider.Context context) {
        elytraModel = new ElytraModel(context.getModelSet().bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, CustomPlayerEntity animatable, GeoRenderData renderData, int packedLight, int overlay) {
        var player = animatable.getEntity();
        ItemStack stack = EquipmentUtil.getEquippedElytraItem(player);
        // TODO: 鞘翅骨骼定位渲染尚未在新版实现
    }
}
