package com.elfmcys.ysm.client.renderer;

import com.elfmcys.ysm.capability.ProjectileAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.entity.CustomProjectileEntity;
import com.elfmcys.ysm.geckolib3.geo.GeoProjectilesRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.NotNull;

public class CustomProjectileRenderer extends GeoProjectilesRenderer<Projectile, CustomProjectileEntity> {
    public CustomProjectileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    /**
     * 由 EntityRenderDispatcherMixin 在 submit 阶段调用。
     */
    public void submitProjectile(Projectile entity, float yaw, float partialTick, PoseStack poseStack,
                                 SubmitNodeCollector collector, CameraRenderState cameraState, int packedLight) {
        if (Minecraft.getInstance().player == null || entity.isInvisibleTo(Minecraft.getInstance().player)) {
            return;
        }
        EntityCapabilityHolder.get(entity, ProjectileAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
            cap.checkModelUpdate();
            render(cap, yaw, partialTick, poseStack, collector, packedLight, cameraState);
        });
    }

    @NotNull
    public Identifier getTextureLocation(Projectile entity) {
        return EntityCapabilityHolder.get(entity, ProjectileAnimatableCapabilityProvider.CAP).map(CustomProjectileEntity::getTextureLocation).orElse(MissingTextureAtlasSprite.getLocation());
    }
}
