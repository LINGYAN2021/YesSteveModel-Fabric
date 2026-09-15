package com.elfmcys.ysm.util;

import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.VehicleAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.compat.FirstPersonCompat;
import com.elfmcys.ysm.client.compat.IrisCompat;
import com.elfmcys.ysm.client.compat.touhoulittlemaid.client.TlmClientCompat;
import com.elfmcys.ysm.client.entity.CustomHumanoidEntity;
import com.elfmcys.ysm.client.entity.IPreviewEntity;
import com.elfmcys.ysm.client.renderer.pip.YsmPreviewRenderState;
import com.elfmcys.ysm.client.renderer.replace.EntityRendererReplace;
import com.elfmcys.ysm.geckolib3.geo.GeoReplacedEntityRenderer;
import com.elfmcys.ysm.geckolib3.geo.RenderContext;
import com.elfmcys.ysm.geckolib3.model.AnimatableEntity;
import com.elfmcys.ysm.geckolib3.model.AnimatedGeoModel;
import com.elfmcys.ysm.mixin.client.GuiGraphicsExtractorAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;

@SuppressWarnings("all")
public final class RenderUtil {
    private static boolean renderingInInventory = false;
    private static boolean renderingInPaperDoll = false;
    private static boolean renderingLevel = false;

    /**
     * GUI 预览用的床方块实体缓存（床是方块实体渲染，无法用方块模型渲染）
     */
    @Nullable
    private static BedBlockEntity previewBed = null;

    public static void setRenderingInInventory(boolean value) {
        renderingInInventory = value;
    }

    public static void setRenderingInPaperDoll(boolean renderingEntitiesInPaperDoll) {
        RenderUtil.renderingInPaperDoll = renderingEntitiesInPaperDoll;
    }

    public static void setRenderingLevel(boolean renderingLevel) {
        RenderUtil.renderingLevel = renderingLevel;
    }

    public static boolean isRenderingLevel() {
        RenderSystem.assertOnRenderThread();
        return renderingLevel;
    }

    public static RenderContext extractRenderContext() {
        RenderSystem.assertOnRenderThread();
        return new RenderContext(
                renderingLevel && !FirstPersonCompat.isRenderingPlayer(),
                IrisCompat.isRenderingShadow(),
                FirstPersonCompat.isRenderingPlayer(),
                renderingInInventory,
                renderingInPaperDoll,
                false,
                false);
    }

    public static void adjustPassengerPosition(Entity entity, PoseStack poseStack, float partialTicks) {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            EntityCapabilityHolder.get(vehicle, VehicleAnimatableCapabilityProvider.CAP).ifPresent(vehicleCap -> {
                if (!vehicleCap.isInitialized() || !vehicleCap.isModelPresent()) {
                    return;
                }
                int index = vehicle.getPassengers().indexOf(entity);
                if (index < 0) {
                    return;
                }
                AnimatedGeoModel loadedGeoModel = vehicleCap.getLoadedGeoModel();
                // TODO
//                if (loadedGeoModel == null || loadedGeoModel.passengerBones().isEmpty() || index >= loadedGeoModel.passengerBones().size()) {
//                    return;
//                }
//                var bone = loadedGeoModel.passengerBones().get(index);
//                if (bone == null) {
//                    return;
//                }
                float rawVehicleYaw = Mth.lerp(partialTicks, vehicle.yRotO, vehicle.getYRot());
                float vehicleYaw = EntityRendererReplace.getYaw(vehicle, rawVehicleYaw, partialTicks);
                poseStack.mulPose(Axis.YP.rotationDegrees(180 - vehicleYaw));
                // TODO
               // RenderUtils.prepMatrixForLocator(poseStack, bone);
                poseStack.mulPose(Axis.YN.rotationDegrees(180 - vehicleYaw));

                // 26.1.2：骑乘偏移改为附件点形式，返回值是世界坐标，需要减去载具自身高度
                double yOffset = -(vehicle.getPassengerRidingPosition(entity).y - vehicle.getY());
                // 如果乘客带有玩家 cap 或者女仆 cap，那么不扣除 0.5 偏移
                boolean playerHasCap = entity instanceof Player player && EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).isPresent();
                if (playerHasCap || TlmClientCompat.hasMaidCap(entity)) {
                    yOffset = yOffset - 0.5;
                }
                poseStack.translate(0, yOffset, 0);
            });
        }
    }

    /**
     * 把自定义 PiP 渲染状态提交给 GUI 渲染状态队列。
     * 反混淆 26.1.2 的 GuiGraphicsExtractor 没有公开提交方法，需要访问器。
     */
    public static void submitPreview(GuiGraphicsExtractor graphics, YsmPreviewRenderState state) {
        ((GuiGraphicsExtractorAccessor) graphics).ysm$guiRenderState().addPicturesInPictureState(state);
    }

    /**
     * 提交自定义 GUI 元素（如轮盘扇形），参考官方 2.6.5 的实现。
     */
    public static void submitGuiElement(GuiGraphicsExtractor graphics, GuiElementRenderState state) {
        ((GuiGraphicsExtractorAccessor) graphics).ysm$guiRenderState().addGuiElement(state);
    }

    @Nullable
    private static final java.util.Deque<ScreenRectangle> SCISSOR_STACK = new java.util.ArrayDeque<>();

    /**
     * 26.1.2 的 GuiGraphicsExtractor.scissorStack 字段类型是包私有内部类，
     * mixin accessor 无法以 Object 取值，因此自行跟踪我们设置的裁剪区域。
     */
    public static void pushScissor(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1) {
        graphics.enableScissor(x0, y0, x1, y1);
        var rect = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0);
        var top = SCISSOR_STACK.peek();
        SCISSOR_STACK.push(top == null ? rect : rect.intersection(top));
    }

    public static void popScissor(GuiGraphicsExtractor graphics) {
        graphics.disableScissor();
        if (!SCISSOR_STACK.isEmpty()) {
            SCISSOR_STACK.pop();
        }
    }

    public static ScreenRectangle peekScissor(GuiGraphicsExtractor graphics) {
        return SCISSOR_STACK.peek();
    }

    /**
     * 模型/动画预览界面（PlayerTextureScreen）的实体渲染。
     * <p>
     * 26.1.2 移植：立即渲染改为提交 {@link YsmPreviewRenderState}，
     * 真正的渲染延迟到 PiP 渲染器中执行，实体字段的修改也在那时进行。
     *
     * @param area    预览区域（GUI 坐标）
     * @param scale   缩放（每方块像素数）
     * @param offsetX 用户拖拽的平移（GUI 像素）
     * @param offsetY 用户拖拽的平移（GUI 像素）
     */
    public static <T extends LivingEntity, TAnimatable extends AnimatableEntity<T> & IPreviewEntity> void renderTextureScreenEntity(
            GuiGraphicsExtractor graphics, ScreenRectangle area, float scale,
            float pitch, float yaw, float offsetX, float offsetY, float partialTick,
            TAnimatable entity, GeoReplacedEntityRenderer<T, ? super TAnimatable> renderer, boolean showGround) {
        var previewInfo = entity.getPreviewInfo();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Level level = entity.getEntity().level();

        List<YsmPreviewRenderState.VanillaEntityEntry> vanillaEntities = new ObjectArrayList<>();
        List<YsmPreviewRenderState.BlockEntry> blocks = new ObjectArrayList<>();
        List<YsmPreviewRenderState.BlockEntityEntry> blockEntities = new ObjectArrayList<>();

        Pose poseOverride = null;
        float bodyRot = 0;
        double localX = 0;
        double localY = 0;
        double localZ = 0;

        if (previewInfo.hasPreview("sleep")) {
            poseOverride = Pose.SLEEPING;
            bodyRot = -90;
            localX = -0.1;
            localY = 0.5625;
            localZ = 0.5;
            BlockState bedState = Blocks.PINK_BED.defaultBlockState();
            blockEntities.add(new YsmPreviewRenderState.BlockEntityEntry(
                    extractBedPart(bedState, BedPart.FOOT, partialTick), new Vector3f(-0.5F, 0, -0.5F)));
            blockEntities.add(new YsmPreviewRenderState.BlockEntityEntry(
                    extractBedPart(bedState, BedPart.HEAD, partialTick), new Vector3f(-0.5F, 0, -1.5F)));
        } else if (previewInfo.hasPreview("swim") || previewInfo.hasPreview("swim_stand")) {
            poseOverride = Pose.SWIMMING;
        } else if (previewInfo.hasPreview("sneak") || previewInfo.hasPreview("sneaking")) {
            poseOverride = Pose.CROUCHING;
            localY = -0.1;
        } else if (previewInfo.hasPreview("sit")) {
            localY = -0.5;
        } else if (previewInfo.hasPreview("ride")) {
            localY = 0.85;
            vanillaEntities.add(extractPreviewEntity(EntityType.HORSE, level, dispatcher, partialTick));
        } else if (previewInfo.hasPreview("ride_pig")) {
            localY = 0.3125;
            vanillaEntities.add(extractPreviewEntity(EntityType.PIG, level, dispatcher, partialTick));
        } else if (previewInfo.hasPreview("boat")) {
            localY = -0.45;
            vanillaEntities.add(extractPreviewEntity(EntityType.CHERRY_BOAT, level, dispatcher, partialTick));
        }

        if (showGround) {
            for (int i = 0; i < 3; i++) {
                for (int j = 1; j < 4; j++) {
                    blocks.add(new YsmPreviewRenderState.BlockEntry(Blocks.GRASS_BLOCK.defaultBlockState(),
                            new Vector3f(i - 1.5F, -1, j - 2.5F)));
                }
            }
            blocks.add(new YsmPreviewRenderState.BlockEntry(Blocks.SHORT_GRASS.defaultBlockState(),
                    new Vector3f(0.5F, 0, -1.5F)));
            blocks.add(new YsmPreviewRenderState.BlockEntry(Blocks.RED_TULIP.defaultBlockState(),
                    new Vector3f(0.5F, 0, -0.5F)));
        }

        PoseStack pose = new PoseStack();
        // PiP 默认把实体原点放在纹理底部中央，向上平移半个区域高度使其居中
        pose.translate(offsetX / scale, offsetY / scale - area.height() / (2F * scale), 0);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(new Quaternionf().rotationXYZ(
                (float) Math.toRadians(-10 + pitch), (float) Math.toRadians(yaw), 0.0F));
        Matrix4f poseMatrix = new Matrix4f(pose.last().pose());

        Pose finalPoseOverride = poseOverride;
        float finalBodyRot = bodyRot;
        double finalLocalX = localX;
        double finalLocalY = localY;
        double finalLocalZ = localZ;
        YsmPreviewRenderState.YsmRenderTask task = (poseStack, collector, cameraState) -> {
            T living = entity.getEntity();
            EntitySnapshot snapshot = EntitySnapshot.capture(living, false);
            living.yBodyRot = finalBodyRot;
            living.yBodyRotO = finalBodyRot;
            living.setYRot(0);
            living.yRotO = 0;
            living.setXRot(0);
            living.xRotO = 0;
            living.yHeadRot = 0;
            living.yHeadRotO = 0;
            if (finalPoseOverride != null) {
                living.setPose(finalPoseOverride);
            }
            poseStack.pushPose();
            poseStack.translate(finalLocalX, finalLocalY, finalLocalZ);
            renderer.renderAnimatableEntity(entity, 0, partialTick, poseStack, collector, 0xf000f0, cameraState);
            poseStack.popPose();
            snapshot.restore(living);
        };

        submitPreview(graphics, YsmPreviewRenderState.of(YsmPreviewRenderState.Lane.TEXTURE_PREVIEW,
                vanillaEntities, task, blocks, blockEntities,
                poseMatrix, area, scale, peekScissor(graphics), false));
    }

    /**
     * 模型选择界面（CatalogModelButton 等）的实体渲染。
     */
    public static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>> void renderModelInGui(
            GuiGraphicsExtractor graphics, ScreenRectangle area, float scale, float partialTick,
            TAnimatable animatableEntity, GeoReplacedEntityRenderer<T, TAnimatable> renderer,
            boolean disablePreviewRotation, boolean disableEquipments) {
        renderModelInGui(graphics, area, scale, partialTick, animatableEntity, renderer,
                disablePreviewRotation, disableEquipments, YsmPreviewRenderState.Lane.MODEL_GRID);
    }

    public static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>> void renderModelInGui(
            GuiGraphicsExtractor graphics, ScreenRectangle area, float scale, float partialTick,
            TAnimatable animatableEntity, GeoReplacedEntityRenderer<T, TAnimatable> renderer,
            boolean disablePreviewRotation, boolean disableEquipments, YsmPreviewRenderState.Lane lane) {
        submitPreview(graphics, YsmPreviewRenderState.of(lane,
                List.of(),
                modelInGuiTask(partialTick, animatableEntity, renderer, disableEquipments),
                List.of(), List.of(),
                poseMatrixFor(area, scale, disablePreviewRotation, animatableEntity), area, scale,
                peekScissor(graphics), false));
    }

    /**
     * 模型卡片预览的渲染任务。
     */
    private static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>>
    YsmPreviewRenderState.YsmRenderTask modelInGuiTask(
            float partialTick, TAnimatable animatableEntity,
            GeoReplacedEntityRenderer<T, TAnimatable> renderer, boolean disableEquipments) {
        // 实体朝向 180：渲染器会转 (180 - yaw) = 0，再由姿态里的 Z180 翻转到正对镜头；
        // 3/4 侧面视角由姿态里的 Y(20°) 提供
        return (poseStack, collector, cameraState) ->
                renderModel(animatableEntity, 180, partialTick, renderer, disableEquipments,
                        poseStack, collector, cameraState);
    }

    /**
     * 单卡片预览的姿态矩阵。
     * <p>
     * PiP 把模型原点（脚部）放在区域底部中央，所以要把它抬到"卡片中心 + 半个模型高度"
     * 才能让整只模型居中；这里按实体包围盒 × heightScale 算真实半高，
     * 固定抬半格区域高度会让模型偏高、只露出下半身。
     */
    private static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>>
    Matrix4f poseMatrixFor(ScreenRectangle area, float scale, boolean disablePreviewRotation,
                           TAnimatable animatableEntity) {
        float areaHeight = area.height() / scale;          // 卡片高度（格）
        // 靠下对齐：脚底略低于卡片底边（脚踝会被裁掉一点），整体视觉最稳
        float feet = areaHeight * 1.04F;
        PoseStack pose = new PoseStack();
        pose.translate(0, feet - areaHeight, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(20));
        pose.mulPose(Axis.XP.rotationDegrees(disablePreviewRotation ? 0 : -10));
        return new Matrix4f(pose.last().pose());
    }

    private static <T extends LivingEntity, TAnimatable extends CustomHumanoidEntity<T>>
    void renderModel(TAnimatable animatableEntity, float yRotGui, float partialTick,
                     GeoReplacedEntityRenderer<T, TAnimatable> renderer, boolean disableEquipments,
                     PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector,
                     net.minecraft.client.renderer.state.level.CameraRenderState cameraState) {
        T living = animatableEntity.getEntity();
        EntitySnapshot snapshot = EntitySnapshot.capture(living, disableEquipments);
        if (disableEquipments) {
            clearEquipment(living);
        }
        living.yBodyRot = yRotGui;
        living.yBodyRotO = yRotGui;
        living.setYRot(yRotGui);
        living.yRotO = yRotGui;
        living.setXRot(0);
        living.xRotO = 0;
        living.yHeadRot = yRotGui;
        living.yHeadRotO = yRotGui;

        // 修正骑乘时 GUI 界面歪头的 bug
        Float vehicleYRot = null;
        if (living.getVehicle() instanceof LivingEntity vehicle) {
            vehicleYRot = vehicle.getYRot();
            living.yHeadRot = vehicleYRot;
            living.yHeadRotO = vehicleYRot;
        }

        poseStack.pushPose();
        if (vehicleYRot != null) {
            poseStack.mulPose(Axis.YP.rotationDegrees(vehicleYRot - yRotGui));
        }
        renderer.renderAnimatableEntity(animatableEntity, 0, partialTick, poseStack, collector, 0xf000f0, cameraState);
        poseStack.popPose();
        snapshot.restore(living);
    }

    /**
     * HUD/配置界面的额外玩家纸娃娃渲染。
     * <p>
     * 走原版 EntityRenderDispatcher.submit（YSM 启用时会被 mixin 替换为自定义模型），
     * z 参数在 extract/submit 管线中没有意义（绘制顺序由提交顺序决定），仅为兼容保留。
     */
    public static void renderExtraPlayerEntity(GuiGraphicsExtractor graphics, LocalPlayer player,
                                               double posX, double posY, float scale, float yawOffset,
                                               int z, float partialTick) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderState state = dispatcher.extractEntity(player, partialTick);
        state.x = 0;
        state.y = 0;
        state.z = 0;
        state.lightCoords = 0xf000f0;
        state.shadowRadius = 0;
        state.shadowPieces.clear();
        state.nameTag = null;

        float bodyRot = state instanceof LivingEntityRenderState livingState ? livingState.bodyRot : 0;
        ScreenRectangle area = new ScreenRectangle((int) (posX - scale / 2), (int) (posY - scale / 2),
                (int) (2 * scale), (int) (3 * scale));

        PoseStack pose = new PoseStack();
        // 旧版实现中人物脚部锚点在 (posX + scale/2, posY + 2*scale)，换算成居中后的偏移
        pose.translate(0, -area.height() / (2F * scale) + 1, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(bodyRot + yawOffset - 180));
        Matrix4f poseMatrix = new Matrix4f(pose.last().pose());

        submitPreview(graphics, YsmPreviewRenderState.of(YsmPreviewRenderState.Lane.PAPER_DOLL,
                List.of(new YsmPreviewRenderState.VanillaEntityEntry(state)), null, List.of(), List.of(),
                poseMatrix, area, scale, peekScissor(graphics), true));
    }

    /**
     * 提取 GUI 预览用的附加实体（马/猪/船）渲染状态，
     * 坐标统一归零（PiP 内为局部坐标），并关闭阴影与名牌。
     */
    private static YsmPreviewRenderState.VanillaEntityEntry extractPreviewEntity(
            EntityType<? extends Entity> type, Level level, EntityRenderDispatcher dispatcher, float partialTick) {
        Entity entity;
        try {
            entity = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.getKey(type),
                    () -> type.create(level, EntitySpawnReason.LOAD));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        EntityRenderState state = dispatcher.extractEntity(entity, partialTick);
        state.x = 0;
        state.y = 0;
        state.z = 0;
        state.lightCoords = 0xf000f0;
        state.shadowRadius = 0;
        state.shadowPieces.clear();
        state.nameTag = null;
        if (state instanceof LivingEntityRenderState livingState) {
            livingState.yRot = 0;
            livingState.bodyRot = 0;
            livingState.xRot = 0;
        }
        return new YsmPreviewRenderState.VanillaEntityEntry(state);
    }

    /**
     * 提取床的一半（床头/床尾）渲染状态
     */
    private static BlockEntityRenderState extractBedPart(BlockState bedState, BedPart part, float partialTick) {
        if (previewBed == null) {
            previewBed = BlockEntityType.BED.create(BlockPos.ZERO, bedState);
        }
        previewBed.setBlockState(bedState.setValue(BedBlock.PART, part));
        return extractBlockEntity(previewBed, partialTick);
    }

    @SuppressWarnings("unchecked")
    private static <E extends net.minecraft.world.level.block.entity.BlockEntity, S extends BlockEntityRenderState> S extractBlockEntity(E blockEntity, float partialTick) {
        BlockEntityRenderer<E, S> renderer = (BlockEntityRenderer<E, S>) Minecraft.getInstance()
                .getBlockEntityRenderDispatcher().getRenderer(blockEntity);
        S state = renderer.createRenderState();
        renderer.extractRenderState(blockEntity, state, partialTick, Vec3.ZERO, null);
        state.lightCoords = 0xf000f0;
        return state;
    }

    private static void clearEquipment(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            entity.setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    /**
     * 实体字段快照：渲染任务执行前保存，执行后恢复，
     * 避免 GUI 预览对实体的修改泄漏到游戏逻辑。
     */
    private static final class EntitySnapshot {
        private final float yBodyRot;
        private final float yBodyRotO;
        private final float yRot;
        private final float yRotO;
        private final float xRot;
        private final float xRotO;
        private final float yHeadRot;
        private final float yHeadRotO;
        private final Pose pose;
        @Nullable
        private final EnumMap<EquipmentSlot, ItemStack> equipment;

        private EntitySnapshot(LivingEntity entity, boolean withEquipment) {
            this.yBodyRot = entity.yBodyRot;
            this.yBodyRotO = entity.yBodyRotO;
            this.yRot = entity.getYRot();
            this.yRotO = entity.yRotO;
            this.xRot = entity.getXRot();
            this.xRotO = entity.xRotO;
            this.yHeadRot = entity.yHeadRot;
            this.yHeadRotO = entity.yHeadRotO;
            this.pose = entity.getPose();
            if (withEquipment) {
                this.equipment = new EnumMap<>(EquipmentSlot.class);
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    this.equipment.put(slot, entity.getItemBySlot(slot));
                }
            } else {
                this.equipment = null;
            }
        }

        static EntitySnapshot capture(LivingEntity entity, boolean withEquipment) {
            return new EntitySnapshot(entity, withEquipment);
        }

        void restore(LivingEntity entity) {
            entity.yBodyRot = this.yBodyRot;
            entity.yBodyRotO = this.yBodyRotO;
            entity.setYRot(this.yRot);
            entity.yRotO = this.yRotO;
            entity.setXRot(this.xRot);
            entity.xRotO = this.xRotO;
            entity.yHeadRot = this.yHeadRot;
            entity.yHeadRotO = this.yHeadRotO;
            entity.setPose(this.pose);
            if (this.equipment != null) {
                this.equipment.forEach(entity::setItemSlot);
            }
        }
    }
}
