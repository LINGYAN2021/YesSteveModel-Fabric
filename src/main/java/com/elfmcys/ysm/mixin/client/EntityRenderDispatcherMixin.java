package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.compat.FirstPersonCompat;
import com.elfmcys.ysm.client.compat.PlayerAnimatorCompat;
import com.elfmcys.ysm.client.compat.realcamera.RealCameraCompat;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.elfmcys.ysm.client.renderer.RenderStateEntityLink;
import com.elfmcys.ysm.client.renderer.replace.EntityRendererReplace;
import com.elfmcys.ysm.client.renderer.replace.FishingHookRendererReplace;
import com.elfmcys.ysm.client.renderer.replace.ProjectileRendererReplace;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.util.PersonView;
import com.elfmcys.ysm.util.RenderUtil;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    /**
     * 26.1.2 中世界渲染与 GUI 纸娃娃渲染都会经过
     * EntityRenderDispatcher.submit -> EntityRenderer.submit，
     * 在这里统一拦截并替换为 YSM 的原生渲染提交。
     */
    @WrapWithCondition(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;" +
                             "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;" +
                             "Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
            )
    )
    private boolean replaceRender(EntityRenderer<?, ?> renderer, EntityRenderState renderState,
                                  PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (!YesSteveModel.isAvailable()) {
            return true;
        }

        Entity entity = RenderStateEntityLink.entityOf(renderState);
        if (entity == null) {
            return true;
        }
        float partialTick = RenderStateEntityLink.partialTickOf(renderState);
        int packedLight = renderState.lightCoords;
        float yaw = renderState instanceof net.minecraft.client.renderer.entity.state.LivingEntityRenderState livingState
                ? livingState.bodyRot
                : renderState instanceof net.minecraft.client.renderer.entity.state.MinecartRenderState minecartState
                ? minecartState.yRot : entity.getYRot();

        if (entity instanceof Projectile projectile && !ClientConfig.DISABLE_PROJECTILE_MODEL.get()) {
            // 鱼漂比较特殊
            if (projectile instanceof FishingHook hook) {
                return FishingHookRendererReplace.renderInMixin(hook, yaw, partialTick, poseStack, collector, cameraState, packedLight);
            } else {
                return ProjectileRendererReplace.renderInMixin(projectile, yaw, partialTick, poseStack, collector, cameraState, packedLight);
            }
        }

        // 界面纸娃娃（背包/模型选择界面）的朝向由渲染状态给出（跟随鼠标），
        // 而 YSM 的原生渲染用的是实体自身朝向，这里补上两者差值，
        // 否则娃娃只会侧着（跟着玩家真实朝向转）
        if (!RenderUtil.isRenderingLevel() && entity instanceof LivingEntity livingEntity
                && renderState instanceof net.minecraft.client.renderer.entity.state.LivingEntityRenderState living) {
            float stateRot = living.bodyRot;
            float entityRot = net.minecraft.util.Mth.lerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot);
            if (stateRot != entityRot) {
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(entityRot - stateRot));
            }
        }

        if (entity instanceof Player player) {
            return replacePlayerRender(player, partialTick, poseStack, collector, cameraState);
        }

        if (!ClientConfig.DISABLE_VEHICLE_MODEL.get()) {
            RenderUtil.adjustPassengerPosition(entity, poseStack, partialTick);
            return EntityRendererReplace.renderInMixin(entity, yaw, partialTick, poseStack, collector, cameraState, packedLight);
        }

        return true;
    }

    /**
     * 原 RenderPlayerEvent.Pre 的处理逻辑
     */
    private boolean replacePlayerRender(Player playerRender, float partialTick, PoseStack poseStack,
                                        SubmitNodeCollector collector, CameraRenderState cameraState) {
        LocalPlayer playerSelf = Minecraft.getInstance().player;
        if (playerRender.equals(playerSelf) && ClientConfig.DISABLE_SELF_MODEL.get()) {
            return true;
        }
        if (!playerRender.equals(playerSelf) && ClientConfig.DISABLE_OTHER_MODEL.get()) {
            return true;
        }
        if (playerRender.isSpectator()) {
            return true;
        }
        return EntityCapabilityHolder.get(playerRender, PlayerAnimatableCapabilityProvider.CAP).map(cap -> {
            boolean enabled = cap.isInitializedAndEnabled();
            if (enabled) {
                if (!PersonView.isFirstPersonView(cap)
                        || FirstPersonCompat.isRenderingPlayer()
                        || RealCameraCompat.isActive()
                        || (ClientConfig.DISABLE_EXTERNAL_FIRST_PERSON_ANIM.get() || !PlayerAnimatorCompat.hasThirdPersonModelAnim(playerSelf))) {
                    RegisterEntityRenderersEvent.getPlayerRenderer().submitPlayer(playerRender, partialTick, poseStack, collector, cameraState);
                    return false;
                }
            }
            return true;
        }).orElse(true);
    }
}
