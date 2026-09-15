package com.elfmcys.ysm.geckolib3.geo;

import com.elfmcys.ysm.geckolib3.core.util.Color;
import com.elfmcys.ysm.geckolib3.model.AnimatableEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public abstract class GeoProjectilesRenderer<TEntity extends Projectile, T extends AnimatableEntity<TEntity>> extends EntityRenderer<TEntity, EntityRenderState> implements IGeoRenderer<T> {
    public GeoProjectilesRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    public void render(T animatable, float yaw, float partialTick, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, CameraRenderState cameraState) {
        var mc = Minecraft.getInstance();
        var data = animatable.update(partialTick);
        if (data != null && mc.player != null) {
            var entity = animatable.getEntity();
            var bodyVisible = !entity.isInvisibleTo(mc.player);
            var glowing = mc.shouldEntityAppearGlowing(entity);
            var renderType = getRenderType(data.texture, bodyVisible, glowing,
                    data.modelState.hasTranslucentVertices());

            if (renderType != null && (bodyVisible || glowing)) {
                poseStack.pushPose();
                try {
                    poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
                    preRender(data, animatable, poseStack, collector,
                            packedLight, getPackedOverlay(entity, 0), Color.WHITE);
                    if (data.modelState.isValid()) {
                        render(data, animatable, renderType, poseStack, collector,
                                packedLight, getPackedOverlay(entity, 0), Color.WHITE);
                    }
                    postRender(data, animatable, poseStack, collector,
                            packedLight, getPackedOverlay(entity, 0), Color.WHITE);
                } finally {
                    poseStack.popPose();
                }
            }
        }
        renderNameTag(animatable.getEntity(), poseStack, collector, cameraState, packedLight);
    }

    protected void renderNameTag(TEntity entity, PoseStack poseStack, SubmitNodeCollector collector,
                                 CameraRenderState cameraState, int packedLight) {
        var displayName = entity.getCustomName();
        if (displayName == null) {
            return;
        }
        double distance = this.entityRenderDispatcher.distanceToSqr(entity);
        if (!this.shouldShowName(entity, distance)) {
            return;
        }
        Vec3 offset = new Vec3(0, entity.getBbHeight() + 0.5F, 0);
        collector.submitNameTag(poseStack, offset, 0, displayName, !entity.isDiscrete(), packedLight, distance, cameraState);
    }

    public static int getPackedOverlay(Entity entity, float uIn) {
        return OverlayTexture.pack(OverlayTexture.u(uIn), OverlayTexture.v(false));
    }
}
