package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.util.RenderUtil;
import com.elfmcys.ysm.capability.AuthModelsCapabilityProvider;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.StarModelsCapabilityProvider;
import com.elfmcys.ysm.client.event.DownloadScreenInterModEvent;
import com.elfmcys.ysm.client.gui.button.CatalogModelButton;
import com.elfmcys.ysm.client.gui.button.FlatColorButton;
import com.elfmcys.ysm.client.gui.button.FlatIconButton;
import com.elfmcys.ysm.client.gui.button.PackButton;
import com.elfmcys.ysm.client.gui.button.StarButton;
import com.elfmcys.ysm.client.input.PlayerModelScreenKey;
import com.elfmcys.ysm.client.model.ModelPackInfo;
import com.elfmcys.ysm.client.model.ModelRenderTarget;
import com.elfmcys.ysm.client.model.ClientAssetBatch;
import com.elfmcys.ysm.client.model.catalog.ClientCatalogSnapshot;
import com.elfmcys.ysm.client.model.ClientModelService;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.model.domain.Hash256;
import com.elfmcys.ysm.model.source.PackOffer;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.fabric.ClientProtocolGateway;
import com.elfmcys.ysm.task.TaskScope;
import com.elfmcys.ysm.util.ModelIdUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import com.elfmcys.ysm.mixin.client.ScreenAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.event.RegisterEntityRenderersEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public class PlayerModelScreen extends Screen {
    private final ClientModelService service = ClientModelService.instance();
    private final CatalogBrowserState browser = new CatalogBrowserState();
    private final HashSet<String> clientNotDisplayModels = new HashSet<>();
    private final List<CatalogModelButton> modelButtons = new ArrayList<>();
    private final List<PackButton> packButtons = new ArrayList<>();
    private final Map<String, PackOffer> packDescriptors = new LinkedHashMap<>();
    private TaskScope pageScope;
    private EditBox textField;
    protected int x;
    protected int y;

    public PlayerModelScreen() {
        super(Component.literal("YSM Player Model GUI"));
        if (NetworkHandler.isRemoteChannelPresent()) {
            clientNotDisplayModels.addAll(ServerConfig.CLIENT_NOT_DISPLAY_MODEL_PATHS.get());
        }
        rebuildCatalog(service.catalog());
    }

    protected PlayerTextureScreen getTextureScreen(PlayerModelScreen parent, Hash256 modelHash,
                                                    ModelRenderTarget renderTarget) {
        return new PlayerTextureScreen(parent, modelHash, renderTarget);
    }

    protected ModelInfoScreen getModelInfoScreen(PlayerModelScreen parent, ModelRenderTarget model) {
        return new ModelInfoScreen(parent, model);
    }

    protected void selectModel(Hash256 hash, String path, String texture, ModelRenderTarget renderTarget) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(capability -> {
            if (NetworkHandler.isRemoteChannelPresent()) {
                if (capability.hasRoamingStorage(hash.roamingHash())) {
                    capability.updateModelAndTexture(hash, texture);
                }
                ClientProtocolGateway.selectModel(hash, texture);
            } else {
                capability.updateModelAndTexture(hash, texture);
            }
        });
    }

    @Override
    protected void init() {
        closePage();
        clearWidgets();
        if (browser.catalog() != service.catalog()) {
            rebuildCatalog(service.catalog());
        }
        calculateModelList();
        pageScope = service.openRequestScope();

        x = (width - 420) / 2;
        y = (height - 235) / 2;
        var previousSearch = textField == null ? "" : textField.getValue();
        var focused = textField != null && textField.isFocused();
        textField = new EditBox(font, x + 144, y + 6, 140, 16, Component.literal("YSM Search Box"));
        textField.setValue(previousSearch);
        textField.setTextColor(0xFFF3EFE0);
        textField.setFocused(focused);
        textField.moveCursorToEnd(false);
        addWidget(textField);

        addHeaderButtons();
        addPageButtons();
        var assets = service.createAssetBatch(pageScope);
        addCatalogButtons(assets);
        assets.submit();
    }

    private void addHeaderButtons() {
        addRenderableWidget(new FlatIconButton(x + 5, y + 5, 20, 20, 80, 16, ignored -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(capability -> {
                    var model = capability.getModelRenderTarget();
                    if (model != null && model.info().metadata() != null) {
                        Minecraft.getInstance().setScreen(getModelInfoScreen(this, model));
                    }
                });
            }
        })).setTooltips("gui.yes_steve_model.model.info");
        addRenderableWidget(new FlatIconButton(x + 28, y + 5, 79, 20, 32, 16, ignored -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(capability -> {
                    var model = capability.getModelRenderTarget();
                    if (model != null && capability.getModelHash() != null) {
                        Minecraft.getInstance().setScreen(getTextureScreen(this, capability.getModelHash(), model));
                    }
                });
            }
        })).setTooltips("gui.yes_steve_model.model.texture");
        addRenderableWidget(new StarButton(x + 110, y + 5));

        if (StringUtils.isNotBlank(browser.currentPack())) {
            addRenderableWidget(new FlatIconButton(x + 110, y + 27, 20, 20, 0, 32,
                    ignored -> backToParent()).setTooltips("gui.back"));
        }

        addRenderableWidget(Checkbox.builder(
                        Component.translatable("gui.yes_steve_model.show_model_id_first"), font)
                .pos(x + 5, y - 22)
                .selected(ClientConfig.SHOW_MODEL_ID_FIRST.get())
                .onValueChange((checkbox, selected) -> {
                    ClientConfig.SHOW_MODEL_ID_FIRST.set(selected);
                })
                .build());

        addCategoryButton(x + 328, 32, CatalogBrowserState.Category.ALL, "gui.yes_steve_model.all_models");
        addCategoryButton(x + 308, 48, CatalogBrowserState.Category.AUTH, "gui.yes_steve_model.auth_models");
        addCategoryButton(x + 288, 0, CatalogBrowserState.Category.STAR, "gui.yes_steve_model.star_models");
        addRenderableWidget(new FlatIconButton(x + 397, y + 5, 18, 18, 16, 16,
                ignored -> minecraft.setScreen(new ConfigScreen(this)))
                .setTooltips("gui.yes_steve_model.config"));
        addRenderableWidget(new FlatIconButton(x + 377, y + 5, 18, 18, 0, 16,
                ignored -> DownloadScreenInterModEvent.openDownloadScreen(this))
                .setTooltips("gui.yes_steve_model.download"));
        addRenderableWidget(new FlatIconButton(x + 357, y + 5, 18, 18, 80, 0,
                ignored -> minecraft.setScreen(new OpenModelFolderScreen(this)))
                .setTooltips("gui.yes_steve_model.open_model_folder.open"));
    }

    private void addCategoryButton(int buttonX, int u, CatalogBrowserState.Category target, String tooltip) {
        addRenderableWidget(new FlatIconButton(buttonX, y + 5, 18, 18, u, 0, ignored -> {
            if (browser.category() != target) {
                browser.category(target);
                init();
            }
        }).setTooltips(tooltip));
    }

    private void addPageButtons() {
        addRenderableWidget(new FlatColorButton(x + 198, y + 215, 52, 14,
                Component.translatable("gui.yes_steve_model.pre_page"), ignored -> {
            if (browser.page() > 0) {
                browser.page(browser.page() - 1);
                init();
            }
        }));
        addRenderableWidget(new FlatColorButton(x + 308, y + 215, 52, 14,
                Component.translatable("gui.yes_steve_model.next_page"), ignored -> {
            if (browser.page() < browser.maxPage()) {
                browser.page(browser.page() + 1);
                init();
            }
        }));
    }

    private void addCatalogButtons(ClientAssetBatch assets) {
        var player = minecraft == null ? null : minecraft.player;
        if (player == null) {
            return;
        }
        var auth = EntityCapabilityHolder.get(player, AuthModelsCapabilityProvider.AUTH_MODELS_CAP).orElse(null);
        for (var slot = 0; slot < 10; slot++) {
            var index = slot + browser.page() * 10;
            var xStart = x + 143 + 55 * (slot % 5);
            var yStart = y + 28 + 93 * (slot / 5);
            if (index < browser.packs().size()) {
                var pack = browser.packs().get(index);
                var button = new PackButton(xStart, yStart, 52, 90, pack,
                        packDescriptors.get(pack.hierarchy()), assets, ignored -> {
                    browser.enterPack(pack.hierarchy());
                    init();
                });
                packButtons.add(button);
                addRenderableWidget(button);
                continue;
            }
            index -= browser.packs().size();
            if (index < 0 || index >= browser.models().size()) {
                continue;
            }
            var entry = browser.models().get(index);
            var needAuth = entry.authorizationRequired()
                    && (auth == null || !auth.containModel(entry.modelHash()));
            var button = new CatalogModelButton(xStart, yStart, entry, needAuth,
                    pageScope, assets, this::selectModel,
                    (hash, path, renderTarget) -> Minecraft.getInstance().setScreen(
                            getTextureScreen(this, hash, renderTarget)), slot);
            modelButtons.add(button);
            addRenderableWidget(button);
        }
    }

    private void calculateModelList() {
        var player = minecraft == null ? null : minecraft.player;
        var auth = player == null ? null
                : EntityCapabilityHolder.get(player, AuthModelsCapabilityProvider.AUTH_MODELS_CAP).orElse(null);
        var stars = player == null ? null
                : EntityCapabilityHolder.get(player, StarModelsCapabilityProvider.STAR_MODELS_CAP).orElse(null);
        browser.filter(textField == null ? "" : textField.getValue(), locale(), clientNotDisplayModels,
                hash -> auth != null && auth.containModel(hash),
                hash -> stars != null && stars.containModel(hash));
    }

    private void rebuildCatalog(ClientCatalogSnapshot next) {
        packDescriptors.clear();
        next.packs().forEach(pack -> packDescriptors.putIfAbsent(pack.subject().hierarchy(), pack));
        browser.rebuild(next, this::packInfo);
    }

    private ModelPackInfo packInfo(PackOffer descriptor) {
        var languages = new LinkedHashMap<String, Map<String, String>>();
        descriptor.translations().forEach((locale, text) -> languages.put(locale,
                Map.of("name", text.name(), "description", text.description())));
        return new ModelPackInfo(descriptor.subject().hierarchy(), descriptor.name(), descriptor.description(),
                null, Map.copyOf(languages));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fillGradient(x, y, x + 135, y + 235, 0xFF222222, 0xFF222222);
        graphics.fillGradient(x + 138, y, x + 420, y + 235, 0xFF222222, 0xFF222222);
        graphics.fillGradient(x + 351, y + 7, x + 352, y + 21, 0xFFF3EFE0, 0xFFF3EFE0);
        textField.extractRenderState(graphics, mouseX, mouseY, partialTick);
        renderReferenceEntity(graphics, mouseX, mouseY, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true));

        if (textField.getValue().isEmpty() && !textField.isFocused()) {
            graphics.text(font, Component.translatable("gui.yes_steve_model.search")
                    .withStyle(ChatFormatting.ITALIC), x + 148, y + 10, 0xFF777777);
        }
        var page = "%d/%d".formatted(browser.page() + 1, browser.maxPage() + 1);
        graphics.text(font, page, x + 138 + (282 - font.width(page)) / 2,
                y + 223 - font.lineHeight / 2, 0xFFF3EFE0);
        var version = FabricLoader.getInstance().getModContainer(YesSteveModel.MOD_ID).map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
        graphics.text(font, version, x + 2, y + 226, ChatFormatting.DARK_GRAY.getColor());
        if (!browser.currentPack().isBlank()) {
            graphics.text(font, Component.literal("\uD83D\uDCC2 " + browser.currentPack())
                    .withStyle(ChatFormatting.GRAY), x + 142, y - 12, 0xFFF3EFE0);
        }
        if (service.loadingCount() > 0) {
            var loading = Component.literal(Integer.toString(service.loadingCount()));
            graphics.text(font, loading, x + 414 - font.width(loading), y + 218,
                    ChatFormatting.DARK_GRAY.getColor());
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        ((ScreenAccessor) this).ysm$renderables().stream().filter(FlatIconButton.class::isInstance).map(FlatIconButton.class::cast)
                .forEach(button -> button.renderToolTip(graphics, this, mouseX, mouseY));
        modelButtons.forEach(button -> button.renderTooltip(graphics, this, mouseX, mouseY));
        ((ScreenAccessor) this).ysm$renderables().stream().filter(PackButton.class::isInstance).map(PackButton.class::cast)
                .forEach(button -> button.renderComponentTooltip(graphics, this, mouseX, mouseY));
    }

    protected void renderReferenceEntity(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        RenderUtil.pushScissor(graphics, x + 5, y + 29, x + 130, y + 200);
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x + 5, y + 29, x + 130, y + 200, 70,
                0, mouseX, mouseY, player);
        RenderUtil.popScissor(graphics);

        EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(capability -> {
            var renderTarget = capability.getModelRenderTarget();
            var fallback = capability.getModelHash() == null ? "default" : service.displayPath(capability.getModelHash());
            var name = renderTarget == null ? ModelIdUtil.getFileNameFromPath(fallback)
                    : renderTarget.getDisplayName(ModelIdUtil.getFileNameFromPath(fallback));
            var lines = font.split(FormattedText.of(name), 125);
            var lineY = y + 205;
            for (FormattedCharSequence line : lines) {
                graphics.text(font, line, x + (135 - font.width(line)) / 2, lineY, 0xFFF3EFE0);
                lineY += 10;
            }
        });
    }

    @Override
    public void tick() {
        if (browser.catalog() != service.catalog()) {
            init();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (textField.mouseClicked(event, doubleClick)) {
            setFocused(textField);
            return true;
        }
        if (textField.isFocused()) {
            textField.setFocused(false);
        }
        var handled = super.mouseClicked(event, doubleClick);
        if (!handled && event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !browser.currentPack().isBlank()) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            backToParent();
            return true;
        }
        return handled;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        var previous = textField.getValue();
        if (textField.charTyped(event)) {
            if (!Objects.equals(previous, textField.getValue())) {
                browser.resetPage();
                init();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (PlayerModelScreenKey.PLAYER_MODEL_KEY.matches(event) && !textField.isFocused()) {
            onClose();
            return true;
        }
        var previous = textField.getValue();
        if (textField.keyPressed(event)) {
            if (!Objects.equals(previous, textField.getValue())) {
                browser.resetPage();
                init();
            }
            return true;
        }
        return textField.isFocused() && textField.isVisible() && event.key() != 256
                || super.keyPressed(event);
    }

    @Override
    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            textField.setValue(text);
        } else {
            textField.insertText(text);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double delta = scrollY;
        if (delta != 0 && mouseX > x + 143 && mouseX < x + 430 && mouseY > y + 25 && mouseY < y + 235) {
            if (delta > 0 && browser.page() > 0) {
                browser.page(browser.page() - 1);
                init();
            } else if (delta < 0 && browser.page() < browser.maxPage()) {
                browser.page(browser.page() + 1);
                init();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void resize(int width, int height) {
        var search = textField == null ? "" : textField.getValue();
        super.resize(width, height);
        textField.setValue(search);
    }

    @Override
    public void removed() {
        closePage();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void closeModelButtons() {
        modelButtons.forEach(CatalogModelButton::close);
        modelButtons.clear();
    }

    private void closePage() {
        closeModelButtons();
        packButtons.forEach(PackButton::close);
        packButtons.clear();
        if (pageScope != null) {
            pageScope.close();
            pageScope = null;
        }
    }

    private void backToParent() {
        browser.backToParent();
        init();
    }

    private String locale() {
        return Minecraft.getInstance().getLanguageManager().getSelected();
    }

}
