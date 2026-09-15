package com.elfmcys.ysm.client.renderer;

import com.elfmcys.ysm.capability.PlayerAnimatableCapability;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.compat.swarfare.SWarfareCompat;
import com.elfmcys.ysm.client.compat.touhoulittlemaid.client.TlmClientCompat;
import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import com.elfmcys.ysm.client.gui.CustomGuiPlayerEntity;
import com.elfmcys.ysm.client.renderer.layer.CustomPlayerElytraLayer;
import com.elfmcys.ysm.client.renderer.layer.CustomPlayerHeadLayer;
import com.elfmcys.ysm.client.renderer.layer.CustomPlayerItemInHandLayer;
import com.elfmcys.ysm.client.renderer.layer.CustomParrotOnShoulderLayer;
import com.elfmcys.ysm.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.ysm.event.bus.YsmEventBus;
import com.elfmcys.ysm.geckolib3.geo.GeoReplacedEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CustomPlayerRenderer extends GeoReplacedEntityRenderer<Player, CustomPlayerEntity> {
    private Identifier textureOverride;

    @SuppressWarnings("all")
    public CustomPlayerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        addLayer(new CustomPlayerItemInHandLayer(ctx.getEntityRenderDispatcher().getItemInHandRenderer()));
        addLayer(new CustomPlayerElytraLayer(ctx));
        addLayer(new CustomParrotOnShoulderLayer(ctx));
        addLayer(new CustomPlayerHeadLayer(ctx));
    }

    /**
     * 由 EntityRenderDispatcherMixin 在 submit 阶段调用。
     */
    public void submitPlayer(Player player, float partialTick, PoseStack poseStack,
                             SubmitNodeCollector collector, CameraRenderState cameraState) {
        // 检查卓越前线的隐藏功能
        if (SWarfareCompat.shouldHidePlayerRender(player)) {
            return;
        }

        PlayerAnimatableCapability cap = EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).orElse(null);
        if (cap == null) {
            return;
        }

        cap.checkModelUpdate();
        var event = new SpecialPlayerRenderEvent(player, cap, cap.getModelId());
        textureOverride = event.getTextureLocationOverride();
        if (YsmEventBus.post(event).isCanceled()) {
            return;
        }

        renderAnimatableEntity(cap, event.getTextureLocationOverride(), player.getYRot(), partialTick,
                poseStack, collector, getPackedLightCoords(player, partialTick), cameraState);
    }

    @Override
    public boolean shouldShowName(Player entity, double distance) {
        float renderDistance = entity.isDiscrete() ? 32.0F : 64.0F;
        if (distance >= (double) (renderDistance * renderDistance)) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null) {
                return false;
            }
            boolean invisible = !entity.isInvisibleTo(player);
            if (entity != player) {
                Team team1 = entity.getTeam();
                Team team2 = player.getTeam();
                if (team1 != null) {
                    Team.Visibility team$visibility = team1.getNameTagVisibility();
                    return switch (team$visibility) {
                        case ALWAYS -> invisible;
                        case NEVER -> false;
                        case HIDE_FOR_OTHER_TEAMS ->
                                team2 == null ? invisible : team1.isAlliedTo(team2) && (team1.canSeeFriendlyInvisibles() || invisible);
                        case HIDE_FOR_OWN_TEAM -> team2 == null ? invisible : !team1.isAlliedTo(team2) && invisible;
                    };
                }
            }
            return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && invisible && !entity.isVehicle();
        }
    }

    @NotNull
    public Identifier getTextureLocation(Player pEntity) {
        return textureOverride == null ? EntityCapabilityHolder.get(pEntity, PlayerAnimatableCapabilityProvider.CAP).map(CustomPlayerEntity::getTextureLocation).orElse(MissingTextureAtlasSprite.getLocation()) : textureOverride;
    }

    @Override
    protected void renderNameTag(Player player, Component displayName, PoseStack poseStack,
                                 SubmitNodeCollector collector, CameraRenderState cameraState, int packedLight, float partialTick) {
        if (CustomGuiPlayerEntity.isFakePlayer(player)) {
            return;
        }
        double distance = this.entityRenderDispatcher.distanceToSqr(player);
        if (!shouldShowName(player, distance)) {
            return;
        }
        poseStack.pushPose();
        if (distance < 100) {
            Scoreboard scoreboard = player.level().getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(net.minecraft.world.scores.DisplaySlot.BELOW_NAME);
            if (objective != null) {
                var score = scoreboard.getPlayerScoreInfo(
                        net.minecraft.world.scores.ScoreHolder.forNameOnly(player.getScoreboardName()), objective);
                if (score != null) {
                    Vec3 offset = new Vec3(0, player.getBbHeight() + 0.5F, 0);
                    collector.submitNameTag(poseStack, offset, 0,
                            Component.literal(Integer.toString(score.value())).append(" ").append(objective.getDisplayName()),
                            !player.isDiscrete(), packedLight, distance, cameraState);
                    poseStack.translate(0, 9.0 * 1.15 * 0.025, 0);
                }
            }
        }
        super.renderNameTag(player, displayName, poseStack, collector, cameraState, packedLight, partialTick);
        poseStack.popPose();
    }

    @Override
    protected void setupRotations(Player player, @NotNull PoseStack poseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        super.setupRotations(player, poseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
        // 如果是坐在女仆的实体上，则需要偏移回去（哎，屎山代码+1006）
        Entity vehicle = player.getVehicle();
        if (TlmClientCompat.isChair(vehicle) || TlmClientCompat.isSit(vehicle)) {
            poseStack.translate(0, 0.5, 0);
        }
    }
}
