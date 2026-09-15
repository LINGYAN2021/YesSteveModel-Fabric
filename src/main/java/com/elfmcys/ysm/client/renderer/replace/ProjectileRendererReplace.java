package com.elfmcys.ysm.client.renderer.replace;

import com.elfmcys.ysm.capability.ProjectileAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.projectile.Projectile;

public class ProjectileRendererReplace {
    public static boolean renderInMixin(Projectile entity, float yaw, float partialTick, PoseStack poseStack,
                                        SubmitNodeCollector collector, CameraRenderState cameraState, int packedLight) {
        return EntityCapabilityHolder.get(entity, ProjectileAnimatableCapabilityProvider.CAP).map(cap -> {
            if (cap.isInitialized() && cap.isModelPresent()) {
                RegisterEntityRenderersEvent.getProjectRenderer().render(cap, yaw, partialTick, poseStack, collector, packedLight, cameraState);
                return false;
            }
            return true;
        }).orElse(true);
    }
}
