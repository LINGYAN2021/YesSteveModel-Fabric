package com.elfmcys.ysm.client.entity;

import com.elfmcys.ysm.capability.PlayerAnimatableCapability;
import com.elfmcys.ysm.client.animation.condition.FPArmConditionManager;
import com.elfmcys.ysm.client.model.ModelRenderTargetLease;
import com.elfmcys.ysm.geckolib3.core.builder.Animation;
import com.elfmcys.ysm.geckolib3.core.builder.controller.AnimationControllerData;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.ysm.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.ysm.geckolib3.model.AnimatedGeoBone;
import com.elfmcys.ysm.geckolib3.model.AnimatableEntity;
import com.elfmcys.ysm.model.domain.RenderTargetIds;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CustomFirstPersonArmEntity extends CustomEntity<LocalPlayer> {
    private final PlayerAnimatableCapability mainModelEntity;

    /**
     * 手臂模型里的左右手臂骨骼，按名字解析并缓存；缓存的骨骼属于某个模型实例，
     * 模型热切换后实例变了，需要重新解析（否则隐藏打在旧骨骼上，新模型双臂都显示）
     */
    private AnimatedGeoBone leftArmBone;
    private AnimatedGeoBone rightArmBone;
    private Object armBonesModel = null;

    public CustomFirstPersonArmEntity(LocalPlayer player, PlayerAnimatableCapability mainModelEntity) {
        super(player, false);
        this.mainModelEntity = mainModelEntity;
        updateModelHash(mainModelEntity.getModelHash());
    }

    @Override
    protected void onSetupAnimationController() {
        getModelRenderTarget().playerResources().fpArmControllerFactory().accept(this);
    }

    public PlayerAnimatableCapability getMainModelEntity() {
        return mainModelEntity;
    }

    /**
     * 第一人称只保留主手：手臂模型（models/arm.json）里左右两条手臂是分开的骨骼
     * （LeftArm/RightArm 各自带前臂与手），相机几乎贴在两条手臂之间，
     * 两条都画会在屏幕上叠成一团柱子；官方第一人称实际只能看到主手那一条。
     * 这里在每帧动画应用之后把副手整条链隐藏掉——codeAnimation 正是动画之后的钩子，
     * 与 PlayerAnimatableCapability 隐藏第一人称头部用的是同一个位置。
     *
     * <p>骨骼名优先精确匹配 LeftArm/RightArm；自定义模型命名不统一时
     * 退化为大小写不敏感的包含匹配（"leftarm"/"rightarm"），都找不到才放弃隐藏。
     */
    @Override
    protected void codeAnimation(AnimationEvent<? extends AnimatableEntity<LocalPlayer>> animationEvent,
                                boolean shouldUpdate) {
        super.codeAnimation(animationEvent, shouldUpdate);
        var model = getLoadedGeoModel();
        if (model == null) {
            return;
        }
        if (armBonesModel != model.getModel()) {
            armBonesModel = model.getModel();
            leftArmBone = null;
            rightArmBone = null;
            for (var bone : model.getSortedBones()) {
                var name = bone.getName();
                if ("LeftArm".equals(name)) {
                    leftArmBone = bone;
                } else if ("RightArm".equals(name)) {
                    rightArmBone = bone;
                }
            }
            if (leftArmBone == null || rightArmBone == null) {
                for (var bone : model.getSortedBones()) {
                    var lower = bone.getName().toLowerCase(java.util.Locale.ROOT);
                    if (leftArmBone == null && lower.contains("left") && lower.contains("arm")) {
                        leftArmBone = bone;
                    } else if (rightArmBone == null && lower.contains("right") && lower.contains("arm")) {
                        rightArmBone = bone;
                    }
                }
            }
        }
        var mainIsRight = getEntity().getMainArm() == HumanoidArm.RIGHT;
        if (offArmBone(mainIsRight) != null) {
            offArmBone(mainIsRight).setHidden(true);
        }
    }

    private AnimatedGeoBone offArmBone(boolean mainIsRight) {
        return mainIsRight ? leftArmBone : rightArmBone;
    }

    @Override
    public void checkModelUpdate() {
        if (!Objects.equals(mainModelEntity.getModelHash(), getModelHash())) {
            updateModelHash(mainModelEntity.getModelHash());
            return;
        }
        super.checkModelUpdate();
        var resources = getModelRenderTarget() == null ? null : getModelRenderTarget().playerResources();
        var variant = resources == null ? null : resources.variants().get(mainModelEntity.getTextureName());
        if (variant == null && resources != null) {
            variant = resources.defaultVariant();
        }
        var loadedModel = getLoadedGeoModel();
        if (variant != null && loadedModel != null && loadedModel.getModel() != variant.armModel()) {
            waitForAsyncUpdate();
            setGeoModelInplace(variant.armModel());
        }
    }

    @Override
    protected @Nullable ResourceHolder createResourceHolder(ModelRenderTargetLease lease, boolean isFallback) {
        return new ResourceHolder(lease, isFallback);
    }

    @Override
    protected String requestedTextureName() {
        return mainModelEntity.getTextureName();
    }

    @Override
    protected String requestedRenderTargetId() {
        return RenderTargetIds.PLAYER;
    }

    @Override
    public @Nullable AnimationControllerData getAnimationControllerData(String animationControllerName) {
        return getModelRenderTarget().playerResources().animationControllers().get(animationControllerName);
    }

    @Override
    public Identifier getTextureLocation() {
        return mainModelEntity.getTextureLocation();
    }

    @Override
    public float getWidthScale() {
        return getModelRenderTarget().info().properties().widthScale();
    }

    @Override
    public float getHeightScale() {
        return getModelRenderTarget().info().properties().heightScale();
    }

    @Override
    public @Nullable Animation getAnimation(String name) {
        return getModelRenderTarget().playerResources().fpArmAnimations().get(name);
    }

    public FPArmConditionManager getFPArmConditionManager() {
        return getModelRenderTarget().playerResources().fpArmConditionManager();
    }

    @Override
    protected GeoModel getYsmGeoModel() {
        var resources = Objects.requireNonNull(getModelRenderTarget().playerResources());
        var variant = resources.variants().get(mainModelEntity.getTextureName());
        return (variant == null ? resources.defaultVariant() : variant).armModel();
    }

    @Override
    protected void preAnimationSetup(float seekTime, boolean shouldTick) {
        // 设置 roaming 变量
        getAnimationProcessor().putRemoteStruct(mainModelEntity.getRoamingStruct());
    }
}
