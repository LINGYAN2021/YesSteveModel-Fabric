package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.StarModelsCapabilityProvider;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import com.elfmcys.ysm.client.gui.CustomGuiPlayerEntity;
import com.elfmcys.ysm.client.model.ClientAssetBatch;
import com.elfmcys.ysm.client.model.ModelRenderTarget;
import com.elfmcys.ysm.client.model.catalog.CatalogModelMetadata;
import com.elfmcys.ysm.client.model.catalog.ClientCatalogEntry;
import com.elfmcys.ysm.client.texture.TextureHolder;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.model.domain.Hash256;
import com.elfmcys.ysm.task.TaskContext;
import com.elfmcys.ysm.util.ModelIdUtil;
import com.elfmcys.ysm.util.RenderUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import net.minecraft.client.renderer.RenderPipelines;
import com.elfmcys.ysm.client.renderer.pip.YsmPreviewRenderState;

/** A catalog entry that progresses from loading indicator to preview image to a full local renderTarget. */
public final class CatalogModelButton extends Button implements AutoCloseable {
    private static final Identifier ICON = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/icon.png");
    private static final int CARD_OVERLAY_Z = 3500;
    private static final int TOOLTIP_Z = 4000;

    private final ClientCatalogEntry entry;
    private final CatalogModelMetadata metadata;
    private final CustomGuiPlayerEntity entity;
    private final boolean needAuth;
    private final SelectionHandler selection;
    private final RenderTargetHandler openRenderTarget;
    private final Component pathName;
    private final CatalogModelCardState state;
    private int slot;

    public CatalogModelButton(int x, int y, ClientCatalogEntry entry, boolean needAuth,
                              TaskContext context, ClientAssetBatch assets,
                              CustomGuiPlayerEntity entity, SelectionHandler selection,
                              RenderTargetHandler openRenderTarget) {
        this(x, y, entry, needAuth, context, assets, selection, openRenderTarget, 0);
    }

    public CatalogModelButton(int x, int y, ClientCatalogEntry entry, boolean needAuth,
                              TaskContext context, ClientAssetBatch assets,
                              SelectionHandler selection,
                              RenderTargetHandler openRenderTarget, int slot) {
        this.slot = slot;
        super(x, y, 52, 90, name(CatalogModelMetadata.from(entry)), ignored -> { }, DEFAULT_NARRATION);
        this.entry = entry;
        this.metadata = CatalogModelMetadata.from(entry);
        // 参照旧版 2.6.5：每张卡片独占一个预览实体。
        // 共享实体池会让所有卡片渲染成同一个（最后绑定的）模型
        this.entity = new CustomGuiPlayerEntity();
        this.needAuth = needAuth;
        this.selection = selection;
        this.openRenderTarget = openRenderTarget;
        this.pathName = Component.literal(ModelIdUtil.getFileNameFromPath(metadata.path()));
        this.state = new CatalogModelCardState(context, assets, entry, metadata, entity);
    }

    public Hash256 modelHash() {
        return entry.modelHash();
    }

    public @Nullable ModelRenderTarget renderTarget() {
        return state.renderTarget();
    }

    @Override
    public Component getMessage() {
        return ClientConfig.SHOW_MODEL_ID_FIRST.get() ? pathName : super.getMessage();
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (!needAuth) {
            selection.select(entry.modelHash(), metadata.path(), metadata.defaultTexture(), state.renderTarget());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (needAuth) {
            return false;
        }
        var renderTarget = state.renderTarget();
        if (event.button() == 1 && isMouseOver(event.x(), event.y()) && renderTarget != null) {
            openRenderTarget.open(entry.modelHash(), metadata.path(), renderTarget);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        state.updatePreviewAnimations(isHovered(), isFocused(), Util.getMillis());
        var color = needAuth ? 0x7F000000 : 0xFF434242;
        graphics.fillGradient(getX(), getY(), getX() + width, getY() + height, color, color);
        renderImage(graphics, state.background() != null ? state.background() : state.preview());
        if (state.renderTarget() != null) {
            renderEntity(graphics);
            renderImage(graphics, state.foreground());
        } else if (state.preview() == null) {
            renderLoading(graphics);
        }
        renderName(graphics);
        renderState(graphics);
    }

    public void renderTooltip(GuiGraphicsExtractor graphics, Screen screen, int mouseX, int mouseY) {
        if (!isHovered()) {
            return;
        }
        var locale = Minecraft.getInstance().getLanguageManager().getSelected();
        var input = CatalogModelTooltipFormatter.input(metadata, entry, locale, state.loadError());
        var lines = CatalogModelTooltipFormatter.format(input, Minecraft.getInstance().hasShiftDown(), I18n::get);
        var wrapped = new ArrayList<FormattedCharSequence>();
        for (var line : lines) {
            wrapped.addAll(Minecraft.getInstance().font.split(
                    Component.literal(line.text()).withStyle(line.color()),
                    CatalogModelTooltipFormatter.MAX_WIDTH));
        }
        // 26.1.2 的 setTooltipForNextFrame 是延迟绘制，天然渲染在最顶层
        graphics.setTooltipForNextFrame(Minecraft.getInstance().font, wrapped, mouseX, mouseY);
    }

    @Override
    public void close() {
        state.close();
        // 卡片独占实体：关闭时释放（ticker 移除 + 资源清理）
        entity.release();
    }

    private void renderEntity(GuiGraphicsExtractor graphics) {
        RenderUtil.pushScissor(graphics, getX(), getY(), getX() + width, getY() + height - 20);
        RenderUtil.renderModelInGui(graphics, previewArea(), previewScale(),
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), entity,
                RegisterEntityRenderersEvent.getPlayerRenderer(),
                metadata.info().properties().disablePreviewRotation(), true,
                YsmPreviewRenderState.Lane.modelCard(slot));
        RenderUtil.popScissor(graphics);
    }

    /**
     * 本格的模型预览区域（GUI 像素）
     */
    public ScreenRectangle previewArea() {
        return new ScreenRectangle(getX(), getY(), width, height - 20);
    }

    public CustomGuiPlayerEntity previewEntity() {
        return entity;
    }

    public boolean hasPreviewModel() {
        return state.renderTarget() != null;
    }

    public float previewScale() {
        return 30f;
    }

    public boolean disablePreviewRotation() {
        return metadata.info().properties().disablePreviewRotation();
    }

    private void renderImage(GuiGraphicsExtractor graphics, @Nullable TextureHolder image) {
        if (image == null || image.id().isEmpty()) {
            return;
        }
        // 26.1.2：混合由渲染管线处理，无需手动开关
        graphics.blit(RenderPipelines.GUI_TEXTURED, image.id().get(), getX(), getY(), 0, 0, width, height, width, height);
    }

    private void renderLoading(GuiGraphicsExtractor graphics) {
        var phase = (int) ((Util.getMillis() / 250L) % 4L);
        var dots = ".".repeat(phase);
        graphics.centeredText(Minecraft.getInstance().font, dots, getX() + width / 2,
                getY() + (height - 20) / 2, 0xFFF3EFE0);
    }

    private void renderName(GuiGraphicsExtractor graphics) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> split = font.split(getMessage(), 45);
        if (split.size() > 1) {
            graphics.centeredText(font, split.get(0), getX() + width / 2, getY() + height - 19, 0xFFF3EFE0);
            graphics.centeredText(font, split.get(1), getX() + width / 2, getY() + height - 10, 0xFFF3EFE0);
        } else {
            graphics.centeredText(font, getMessage(), getX() + width / 2, getY() + height - 15, 0xFFF3EFE0);
        }
    }

    private void renderState(GuiGraphicsExtractor graphics) {
        if (!needAuth && isHoveredOrFocused()) {
            graphics.fillGradient(getX(), getY() + 1, getX() + 1, getY() + height - 1, 0xFFF3EFE0, 0xFFF3EFE0);
            graphics.fillGradient(getX(), getY(), getX() + width, getY() + 1, 0xFFF3EFE0, 0xFFF3EFE0);
            graphics.fillGradient(getX() + width - 1, getY() + 1, getX() + width, getY() + height - 1, 0xFFF3EFE0, 0xFFF3EFE0);
            graphics.fillGradient(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFFF3EFE0, 0xFFF3EFE0);
        }
        if (needAuth) {
            graphics.fillGradient(getX(), getY(), getX() + width, getY() + height, 0x9F222222, 0x9F222222);
        }
        var player = Minecraft.getInstance().player;
        if (player != null) {
            EntityCapabilityHolder.get(player, StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(stars -> {
                if (stars.containModel(entry.modelHash())) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, ICON, getX() + width - 14, getY(), 16, 0, 16, 16, 256, 256);
                }
            });
        }
    }

    private static Component name(CatalogModelMetadata metadata) {
        return Component.literal(CatalogModelTooltipFormatter.displayName(metadata,
                Minecraft.getInstance().getLanguageManager().getSelected()));
    }

    @FunctionalInterface
    public interface SelectionHandler {
        void select(Hash256 hash, String path, String texture, @Nullable ModelRenderTarget renderTarget);
    }

    @FunctionalInterface
    public interface RenderTargetHandler {
        void open(Hash256 hash, String path, ModelRenderTarget renderTarget);
    }
}
