package com.elfmcys.ysm.geckolib3.geo;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.api.rendering.v0.TargetKind;
import com.elfmcys.ysm.api.rendering.v0.event.RenderLayerEvent;
import com.elfmcys.ysm.api.rendering.v0.event.RenderModelEvent;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.geckolib3.core.util.Color;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Optional;

/**
 * 26.1.2 移植：原版实体渲染改为 extract/submit 两段式，YSM 渲染器不再作为
 * 原版渲染器注册使用，而是由 EntityRenderDispatcherMixin 在 submit 阶段拦截调用。
 * 因此这里只继承 EntityRenderer 获取 font / dispatcher / 名牌等基础能力，
 * 渲染本身通过 {@link SubmitNodeCollector#submitCustomGeometry} 提交给原生渲染器。
 */
public abstract class GeoReplacedEntityRenderer<TEntity extends LivingEntity, T extends CustomHumanoidEntity<TEntity>> extends EntityRenderer<TEntity, EntityRenderState> implements IGeoRenderer<T> {
    protected final List<GeoLayerRenderer<T>> layerRenderers = new ObjectArrayList<>();

    public GeoReplacedEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    public static int getPackedOverlay(LivingEntity entity, float u) {
        return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
    }

    public void renderAnimatableEntity(T animatableEntity, float entityYaw, float partialTick,
                                       PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                                       CameraRenderState cameraState) {
        renderAnimatableEntity(animatableEntity, null, entityYaw, partialTick, poseStack, collector, packedLight, cameraState);
    }

    public void renderAnimatableEntity(T animatableEntity, @Nullable Identifier textureOverride, float entityYaw, float partialTick,
                                       PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                                       CameraRenderState cameraState) {
        final TEntity entity = animatableEntity.getEntity();
        var mc = Minecraft.getInstance();
        var data = animatableEntity.update(partialTick);
        if (data != null && mc.player != null) {
            poseStack.pushPose();
            try {
                if (entity.getPose() == Pose.SLEEPING) {
                    Direction direction = entity.getBedOrientation();
                    if (direction != null) {
                        float eyeOffset = entity.getEyeHeight(Pose.STANDING) - 0.1f;
                        poseStack.translate(-direction.getStepX() * eyeOffset, 0, -direction.getStepZ() * eyeOffset);
                    }
                }

                setupRotations(entity, poseStack, data.animationData.lerpedAge, data.animationData.lerpBodyRot, partialTick);

                if (animatableEntity.getEntity().getVehicle() != null) {
                    EntityCapabilityHolder.get(animatableEntity.getEntity().getVehicle(), VehicleAnimatableCapabilityProvider.CAP).ifPresent(cap -> {
                        var rot = cap.getRotation();
                        if (rot != null) {
                            poseStack.mulPose(new Quaternionf().rotateZYX(rot.z, 0, rot.x).invert());
                        }
                    });
                }

                poseStack.translate(0, 0.01f, 0);

                var texture = textureOverride == null ? data.texture : textureOverride;
                var bodyVisible = this.isBodyVisible(entity) && !entity.isInvisibleTo(mc.player);
                var glowing = mc.shouldEntityAppearGlowing(entity);
                var renderType = getRenderType(texture,
                        bodyVisible, glowing,
                        data.modelState.hasTranslucentVertices());

                var renderLayersFirst = data.renderLayersFirst;
                var packedOverlay = getPackedOverlay(entity, getOverlayProgress(entity, partialTick));

                if (renderType != null) {
                    preRender(data, animatableEntity, poseStack, collector,
                            packedLight, packedOverlay, Color.WHITE);
                    if (renderLayersFirst && !entity.isSpectator()) {
                        renderLayer(poseStack, collector, animatableEntity, data, packedLight, packedOverlay);
                    }

                    if (data.modelState.isValid()) {
                        var event = new RenderModelEvent(animatableEntity.getEntity(),
                                TargetKind.PLAYER,
                                data,
                                collector,
                                renderType,
                                poseStack,
                                packedLight,
                                packedOverlay,
                                Color.WHITE.getColor());
                        if (!YesSteveModel.postEvent(event)) {
                            render(data, animatableEntity, renderType, poseStack, collector,
                                    packedLight, packedOverlay, Color.WHITE);
                        }
                    }

                    if (!renderLayersFirst && !entity.isSpectator()) {
                        renderLayer(poseStack, collector, animatableEntity, data, packedLight, packedOverlay);
                    }
                    postRender(data, animatableEntity, poseStack, collector,
                            packedLight, packedOverlay, Color.WHITE);
                }
            } finally {
                poseStack.popPose();
            }
        }

        renderNameTag(entity, entity.getDisplayName(), poseStack, collector, cameraState, packedLight, partialTick);
    }

    protected void renderLayer(PoseStack poseStack, SubmitNodeCollector collector, T animatable, GeoRenderData renderData, int packedLight, int overlay) {
        var event = new RenderLayerEvent(animatable.getEntity(),
                TargetKind.PLAYER,
                renderData,
                poseStack,
                collector,
                packedLight,
                overlay);
        if (!YesSteveModel.postEvent(event)) {
            for (GeoLayerRenderer<T> layerRenderer : this.layerRenderers) {
                layerRenderer.render(poseStack, collector, animatable, renderData, packedLight, overlay);
            }
        }
    }

    protected void renderNameTag(TEntity entity, net.minecraft.network.chat.Component displayName, PoseStack poseStack,
                                 SubmitNodeCollector collector, CameraRenderState cameraState, int packedLight, float partialTick) {
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

    protected float getOverlayProgress(TEntity entity, float partialTicks) {
        return 0.0F;
    }

    /**
     * 内联原版 LivingEntityRenderer.setupRotations 的实体参数版本；
     * 与 1.20.1 时代相同，YSM 跳过死亡倒地与鞘翅旋转（由动画系统接管）。
     */
    protected void setupRotations(TEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        float yaw = rotationYaw;
        if (entity.isFullyFrozen()) {
            yaw += (float) (Math.cos(entity.tickCount * 3.25) * Math.PI * 0.4F);
        }

        // 爬梯时，禁止旋转
        if (entity.onClimbable()) {
            Optional<BlockPos> climbablePos = entity.getLastClimbablePos();
            if (climbablePos.isPresent()) {
                BlockState blockState = entity.level().getBlockState(climbablePos.get());
                Optional<Direction> optionalValue = blockState.getOptionalValue(HorizontalDirectionalBlock.FACING);
                if (optionalValue.isPresent()) {
                    yaw = optionalValue.get().getOpposite().get2DDataValue() * 90;
                }
            }
        }

        Pose pose = entity.getPose();
        if (pose != Pose.SLEEPING) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        }

        if (pose == Pose.SLEEPING) {
            Direction direction = entity.getBedOrientation();
            float bedYaw = direction != null ? getFacingAngle(direction) : yaw;
            poseStack.mulPose(Axis.YP.rotationDegrees(bedYaw));
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
        } else if (entity.hasCustomName() || entity instanceof Player) {
            if (isUpsideDown(entity)) {
                poseStack.translate(0.0F, entity.getBbHeight() + 0.1F, 0.0F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
        }
    }

    private static float getFacingAngle(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90.0F;
            case WEST -> 0.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

    private static boolean isUpsideDown(LivingEntity entity) {
        String name = entity instanceof Player player ? player.getGameProfile().name() : entity.getName().getString();
        return "Dinnerbone".equals(name) || "Grumm".equals(name);
    }

    protected boolean isBodyVisible(TEntity entity) {
        boolean visible = !entity.isInvisible();
        boolean spectatorVisible = !visible && !entity.isInvisibleTo(Minecraft.getInstance().player);
        boolean glowing = Minecraft.getInstance().shouldEntityAppearGlowing(entity);
        return visible || spectatorVisible || glowing;
    }

    public boolean shouldShowName(TEntity entity, double distanceSq) {
        return entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity;
    }

    public final boolean addLayer(GeoLayerRenderer<T> layer) {
        return this.layerRenderers.add(layer);
    }

    @Deprecated
    public boolean shouldShowName(TEntity entity) {
        return shouldShowName(entity, this.entityRenderDispatcher.distanceToSqr(entity));
    }

    protected static float lerpAngle(float partialTick, float prev, float current) {
        return Mth.rotLerp(partialTick, prev, current);
    }

}
