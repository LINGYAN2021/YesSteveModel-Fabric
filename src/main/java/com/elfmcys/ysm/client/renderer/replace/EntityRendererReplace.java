package com.elfmcys.ysm.client.renderer.replace;

import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityRendererReplace {
    public static boolean renderInMixin(Entity entity, float yawIn, float partialTick, PoseStack poseStack,
                                        SubmitNodeCollector collector, CameraRenderState cameraState, int packedLight) {
        return EntityCapabilityHolder.get(entity, VehicleAnimatableCapabilityProvider.CAP).map(cap -> {
            if (cap.isInitialized() && cap.isModelPresent()) {
                float yaw = getYaw(entity, yawIn, partialTick);
                RegisterEntityRenderersEvent.getVehicleRenderer().render(cap, yaw, partialTick, poseStack, collector, packedLight, cameraState);
                return false;
            }
            return true;
        }).orElse(true);
    }

    public static float getYaw(Entity entity, float yawIn, float partialTick) {
        float yaw = yawIn;

        if (entity instanceof LivingEntity livingEntity) {
            yaw = getLivingEntityYaw(livingEntity, partialTick);
        }
        // 26.1.2：矿车轨道朝向已在提取阶段计算（MinecartRenderState.yRot），
        // 由 EntityRenderDispatcherMixin 直接取用
        return yaw;
    }

    private static float getLivingEntityYaw(LivingEntity livingEntity, float partialTick) {
        float yaw = Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, livingEntity.yHeadRotO, livingEntity.yHeadRot);

        boolean shouldSit = livingEntity.isPassenger() && (livingEntity.getVehicle() != null);
        if (shouldSit && livingEntity.getVehicle() instanceof LivingEntity vehicle) {
            yaw = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);

            float wrappedYawDiff = Mth.wrapDegrees(headYaw - yaw);
            wrappedYawDiff = Mth.clamp(wrappedYawDiff, -85.0F, 85.0F);

            yaw = headYaw - wrappedYawDiff;
            if (wrappedYawDiff * wrappedYawDiff > 2500.0F) {
                yaw += wrappedYawDiff * 0.2F;
            }
        }
        return yaw;
    }

}
