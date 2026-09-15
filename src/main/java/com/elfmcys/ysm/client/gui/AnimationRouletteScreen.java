package com.elfmcys.ysm.client.gui;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.client.animation.molang.CustomMolangParser;
import com.elfmcys.ysm.client.event.PlayerMoveEvent;
import com.elfmcys.ysm.client.gui.button.*;
import com.elfmcys.ysm.client.input.AnimationRouletteKey;
import com.elfmcys.ysm.client.input.ExtraAnimationKey;
import com.elfmcys.ysm.client.lang.LanguageManager;
import com.elfmcys.ysm.client.model.ModelRenderTarget;
import com.elfmcys.ysm.config.ClientConfig;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.geckolib3.core.molang.value.IValue;
import com.elfmcys.ysm.geckolib3.model.AnimatableEntity;
import com.elfmcys.ysm.info.ModelProperties;
import com.elfmcys.ysm.info.roulette.ExtraAnimationButton;
import com.elfmcys.ysm.info.roulette.forms.CheckboxForms;
import com.elfmcys.ysm.info.roulette.forms.ConfigForms;
import com.elfmcys.ysm.info.roulette.forms.RadioForms;
import com.elfmcys.ysm.info.roulette.forms.RangeForms;
import com.elfmcys.ysm.molang.parser.ParseException;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.fabric.ClientProtocolGateway;
import com.elfmcys.ysm.util.FifoHashMap;
import com.google.common.collect.Lists;
import com.elfmcys.ysm.mixin.client.ScreenAccessor;
import com.elfmcys.ysm.util.RenderUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;

public class AnimationRouletteScreen extends Screen {
    private static final String SPEC_PREFIX = "#";
    private static final String SPEC_RETURN = "#return";

    /**
     * 配置文本的国际化 key 模板
     */
    private static final String CONFIG_TITLE = "properties.extra_animation_buttons.%s.config_forms.%d.title";
    private static final String CONFIG_DESC = "properties.extra_animation_buttons.%s.config_forms.%d.description";
    private static final String CONFIG_LABELS = "properties.extra_animation_buttons.%s.config_forms.%d.labels.%d";

    private static final int MAX_ROULETTE_COUNT = 8;

    /**
     * 用来缓存当前页面的打开情况，用于在每次打开时，都能记住上一次的页数
     */
    private static final LinkedList<Pair<String, Integer>> CACHE = Lists.newLinkedList();
    /**
     * 缓存当前打开轮盘时的 model id，用于重置轮盘数据
     */
    private static String MODEL_ID = "";

    private int x;
    private int y;
    private int selectId = -1;
    private int hoverConfigId = -1;
    private ExtraAnimationButton configButtons = null;
    private Pair<String, Integer> current;

    /**
     * 当前配置页面滚动的 Y 值
     */
    private int configScrollY = 0;
    /**
     * 当前配置页面最大可以滚动的数值
     */
    private int maxScrollY = 0;
    /**
     * 上下滚动配置的按钮
     */
    @Nullable
    private FlatColorButton scrollConfigUpBtn;
    @Nullable
    private FlatColorButton scrollConfigDownBtn;

    private final FifoHashMap<String, String> extraAnimationMap;
    private final Map<String, ExtraAnimationButton> buttonMap;
    private final Map<String, FifoHashMap<String, String>> classifyMap;
    private final ModelProperties modelProperties;
    private final AnimatableEntity<?> animatableEntity;
    private final ModelRenderTarget model;

    public AnimationRouletteScreen(Map<String, ExtraAnimationButton> buttonMap,
                                   Map<String, FifoHashMap<String, String>> classifyMap,
                                   ModelRenderTarget clientModel, AnimatableEntity<?> animatableEntity) {
        super(Component.literal("Animation Roulette GUI"));
        this.model = clientModel;
        this.modelProperties = clientModel.info().properties();
        this.animatableEntity = animatableEntity;
        this.classifyMap = classifyMap;
        this.buttonMap = buttonMap;

        // 缓存的读取
        this.current = CACHE.peekLast();
        if (this.current != null && this.classifyMap.containsKey(current.getLeft())) {
            this.extraAnimationMap = this.classifyMap.get(current.getLeft());
        } else {
            this.extraAnimationMap = this.modelProperties.extraAnimationOrderMap();
            CACHE.clear();
            CACHE.add(MutablePair.of(StringUtils.EMPTY, this.current == null ? 0 : this.current.getRight()));
            this.current = CACHE.peekLast();
        }
    }

    public AnimationRouletteScreen(String modelId, ModelRenderTarget model, AnimatableEntity<?> animatableEntity) {
        super(Component.literal("Animation Roulette GUI"));
        this.model = model;
        this.modelProperties = model.info().properties();
        this.animatableEntity = animatableEntity;
        this.classifyMap = this.modelProperties.extraAnimationClassifyMap();
        this.buttonMap = this.modelProperties.extraAnimationButtonsMap();

        // 如果 ID 和缓存的不一致，重置轮盘缓存
        if (!MODEL_ID.equals(modelId)) {
            CACHE.clear();
            MODEL_ID = modelId;
        }
        // 缓存的读取
        if (CACHE.isEmpty()) {
            CACHE.add(MutablePair.of(StringUtils.EMPTY, 0));
        }
        this.current = CACHE.peekLast();
        if (this.classifyMap.containsKey(current.getLeft())) {
            this.extraAnimationMap = this.classifyMap.get(current.getLeft());
        } else {
            this.extraAnimationMap = this.modelProperties.extraAnimationOrderMap();
            CACHE.clear();
            CACHE.add(MutablePair.of(StringUtils.EMPTY, this.current.getRight()));
            this.current = CACHE.peekLast();
        }
    }

    @Override
    protected void init() {
        this.clearWidgets();

        this.x = width / 2 - 70;
        this.y = height / 2 - 8;
        if (this.extraAnimationMap.size() < (current.getRight() * MAX_ROULETTE_COUNT + 1)) {
            current.setValue(0);
        }
        if (this.extraAnimationMap.size() <= this.selectId) {
            this.selectId = 0;
        }

        if (this.animatableEntity.getEntity() instanceof Player) {
            // 如果是玩家，那么添加锁定按钮
            this.addRenderableWidget(new FlatColorButton(this.x - 20, this.y - 10, 40, 20, Component.empty(), b -> PlayerMoveEvent.switchLock()) {
                @Override
                @NotNull
                public Component getMessage() {
                    if (PlayerMoveEvent.isLocked()) {
                        return Component.translatable("gui.yes_steve_model.roulette.lock_on");
                    }
                    return Component.translatable("gui.yes_steve_model.roulette.lock_off");
                }
            });
        } else {
            // 否则是停止播放轮盘动画按钮
            this.addRenderableWidget(new FlatColorButton(this.x - 20, this.y - 10, 40, 20, Component.translatable("gui.yes_steve_model.roulette.stop"), b -> {
                Entity entity = this.animatableEntity.getEntity();
                if (entity instanceof Player) ClientProtocolGateway.stopSelfAnimation();
                else ClientProtocolGateway.stopMaidAnimation(entity.getId());
                this.onClose();
            }));
        }

        // 翻页按钮
        this.addRenderableWidget(new FlatColorButton(this.x + 125, this.y - 102, 30, 30, Component.literal("<"), b -> this.pageUp()));
        this.addRenderableWidget(new FlatColorButton(this.x + 240, this.y - 102, 30, 30, Component.literal(">"), b -> this.pageDown()));

        // 添加返回按钮
        Component name = Component.translatable("gui.yes_steve_model.model.return");
        this.addRenderableWidget(new FlatColorButton(this.x + 125, this.y - 70, 145, 22, name, b -> this.clickReturn()));

        // 配置按钮
        if (configButtons != null) {
            // 配置上下滚动按钮
            this.scrollConfigUpBtn = new FlatColorButton(this.x + 242, this.y - 46, 28, 60, Component.literal("↑"), b -> {
                this.configScrollUp(50);
                if (this.configScrollY == 0 && this.scrollConfigUpBtn != null) {
                    this.scrollConfigUpBtn.active = false;
                }
                if (this.scrollConfigDownBtn != null) {
                    this.scrollConfigDownBtn.active = true;
                }
            });
            this.scrollConfigDownBtn = new FlatColorButton(this.x + 242, this.y + 50, 28, 60, Component.literal("↓"), b -> {
                this.configScrollDown(50);
                if (this.configScrollY == this.maxScrollY && this.scrollConfigDownBtn != null) {
                    this.scrollConfigDownBtn.active = false;
                }
                if (this.scrollConfigUpBtn != null) {
                    this.scrollConfigUpBtn.active = true;
                }
            });
            this.addRenderableWidget(this.scrollConfigUpBtn);
            this.addRenderableWidget(this.scrollConfigDownBtn);

            final int[] yOffset = {-46};
            final int[] index = {0};
            for (ConfigForms configForm : configButtons.getConfigForms()) {
                this.addConfigForms(configForm, yOffset, index);
            }
        }
    }

    private void addConfigForms(ConfigForms configForm, int[] yOffset, int[] index) {
        if (configForm instanceof CheckboxForms checkboxForms) {
            this.executeMolang(configForm.value(), result -> {
                minecraft.execute(() -> {
                    FlatCheckbox checkbox = getFlatCheckbox(checkboxForms, result, yOffset, index);
                    this.addRenderableWidget(checkbox);
                    yOffset[0] += 14;
                    index[0]++;
                    // 最终和 110 的差就是最大滚动高度
                    this.maxScrollY = Math.max(0, yOffset[0] - 110);
                });
            });
        }

        if (configForm instanceof RangeForms rangeForms) {
            this.executeMolang(configForm.value(), result -> {
                minecraft.execute(() -> {
                    FlatSlider slider = getFlatSlider(rangeForms, result, yOffset, index);
                    this.addRenderableWidget(slider);
                    yOffset[0] += 17;
                    index[0]++;
                    // 最终和 110 的差就是最大滚动高度
                    this.maxScrollY = Math.max(0, yOffset[0] - 110);
                });
            });
        }

        if (configForm instanceof RadioForms radioForms) {
            this.executeMolang(configForm.value(), result -> {
                minecraft.execute(() -> {
                    addRatioButtons(radioForms, result, yOffset, index);
                });
            });
        }
    }

    private void addRatioButtons(RadioForms radioForms, String result, int[] yOffset, int[] index) {
        int selectedIndex = Math.round(transformNumber(result));
        var labels = radioForms.labels();
        if (selectedIndex < 0 || labels.size() < selectedIndex) {
            selectedIndex = 0;
        }

        // 动态改变单选框每行个数
        // 遍历获取最长的行的长度
        int lineMaxWidth = 0;
        int labelsIndex = 0;
        for (String labelName : labels.keyList()) {
            String labelStr = LanguageManager.getI18n(this.model, CONFIG_LABELS.formatted(this.configButtons.getId(), index[0], labelsIndex), labelName);
            lineMaxWidth = Math.max(lineMaxWidth, font.width(labelStr) + 16);
            labelsIndex++;
        }
        if (lineMaxWidth == 0) {
            lineMaxWidth = 115;
        }
        int countPerLine = Math.max(1, 115 / lineMaxWidth);

        String titleStr = LanguageManager.getI18n(this.model, CONFIG_TITLE.formatted(this.configButtons.getId(), index[0]), radioForms.title());
        String descStr = LanguageManager.getI18n(this.model, CONFIG_DESC.formatted(this.configButtons.getId(), index[0]), radioForms.description());

        Component title = Component.literal(titleStr);
        Tooltip description = Tooltip.create(Component.literal(descStr));
        int maxHeight = ((labels.size() - 1) / countPerLine + 1) * 14 + 14;
        FlatRatioBox ratioBox = new FlatRatioBox(this.x + 125, this.y + yOffset[0], maxHeight, title);
        ratioBox.setTooltip(description);
        this.addRenderableOnly(ratioBox);

        // 遍历添加每个 label
        int tempYOffset = yOffset[0] + 14;
        for (int i = 0; i < labels.size(); i++) {
            String labelStr = LanguageManager.getI18n(this.model, CONFIG_LABELS.formatted(this.configButtons.getId(), index[0], i), labels.getKeyAt(i));

            Component labelName = Component.literal(labelStr);
            String labelValue = labels.getValueAt(i);
            boolean isSelected = selectedIndex == i;

            int perWidth = Math.round(110f / countPerLine);
            int xOffset = this.x + 127 + perWidth * (i % countPerLine);

            FlatCheckbox checkbox = new FlatCheckbox(xOffset, this.y + tempYOffset, perWidth, labelName, data -> {
                executeMolang(labelValue, null);
                if (!CustomMolangParser.hasOnlyRoamingAssignment(labelValue) && NetworkHandler.isRemoteChannelPresent() && !ServerConfig.LOW_BANDWIDTH_USAGE.get()) {
                    // 同步到周围的玩家
                    ClientProtocolGateway.submitRouletteExpression(this.animatableEntity.getEntity(), labelValue);
                }
                this.init();
            });
            checkbox.setStateTriggered(isSelected);

            this.addRenderableWidget(checkbox);

            // 每满 countPerLine 个时，换行
            if (i % countPerLine == (countPerLine - 1)) {
                tempYOffset += 14;
            }
        }

        // 最后记得换行
        yOffset[0] = yOffset[0] + maxHeight + 3;
        index[0] = index[0] + 1;

        // 最终和 110 的差就是最大滚动高度
        this.maxScrollY = Math.max(0, yOffset[0] - 110);
    }

    @NotNull
    private FlatSlider getFlatSlider(RangeForms rangeForms, String result, int[] yOffset, int[] index) {
        String titleStr = LanguageManager.getI18n(this.model, CONFIG_TITLE.formatted(this.configButtons.getId(), index[0]), rangeForms.title());
        String descStr = LanguageManager.getI18n(this.model, CONFIG_DESC.formatted(this.configButtons.getId(), index[0]), rangeForms.description());

        Component title = Component.literal(titleStr);
        Tooltip description = Tooltip.create(Component.literal(descStr));
        float number = transformNumber(result);

        FlatSlider slider = new FlatSlider(this.x + 125, this.y + yOffset[0], title, number, this.animatableEntity, rangeForms.value(), rangeForms.step(), rangeForms.min(), rangeForms.max());
        slider.setTooltip(description);

        return slider;
    }

    @NotNull
    private FlatCheckbox getFlatCheckbox(CheckboxForms checkboxForms, String result, int[] yOffset, int[] index) {
        String titleStr = LanguageManager.getI18n(this.model, CONFIG_TITLE.formatted(this.configButtons.getId(), index[0]), checkboxForms.title());
        String descStr = LanguageManager.getI18n(this.model, CONFIG_DESC.formatted(this.configButtons.getId(), index[0]), checkboxForms.description());

        Component title = Component.literal(titleStr);
        Tooltip description = Tooltip.create(Component.literal(descStr));

        float number = transformNumber(result);

        FlatCheckbox checkbox = new FlatCheckbox(this.x + 125, this.y + yOffset[0], title, data -> {
            // 手动拼接 molang 字符串进行赋值操作
            String value = data ? "1" : "0";
            String molang = checkboxForms.value() + "=" + value;
            executeMolang(molang, null);
            if (!CustomMolangParser.hasOnlyRoamingAssignment(molang) && NetworkHandler.isRemoteChannelPresent() && !ServerConfig.LOW_BANDWIDTH_USAGE.get()) {
                // 同步到周围的玩家
                ClientProtocolGateway.submitRouletteExpression(this.animatableEntity.getEntity(), molang);
            }
        }) {
            // 给单选框加上背景
            @Override
            public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
                graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + getHeight(), 0xef_434242);
                super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);
            }
        };
        checkbox.setStateTriggered(number > 0);
        checkbox.setTooltip(description);

        return checkbox;
    }

    private float transformNumber(String result) {
        float number;
        if ("null".equals(result)) {
            number = 0;
        } else if (NumberUtils.isParsable(result)) {
            number = Float.parseFloat(result);
        } else if (BooleanUtils.toBooleanObject(result) != null) {
            number = BooleanUtils.toBoolean(result) ? 1 : 0;
        } else {
            number = 0;
        }
        return number;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        String patText = StringUtils.joinWith(" > ", CACHE.stream().map(Pair::getLeft).toArray());
        graphics.centeredText(font, Component.translatable("gui.yes_steve_model.roulette.path", patText), this.x + 195, this.y - 100, 0xFFFFFFFF);

        this.drawRouletteBg(graphics, mouseX, mouseY);
        this.drawRouletteText(graphics);
        this.drawPageText(graphics);

        // 普通按钮正常渲染
        for (Renderable renderable : ((ScreenAccessor) this).ysm$renderables()) {
            if (!(renderable instanceof IConfigFormsButton)) {
                renderable.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
        }

        RenderUtil.pushScissor(graphics, 0, this.y - 46, this.width, this.y + 110);
        if (mouseY < (this.y - 46) || (this.y + 110) < mouseY) {
            // 鼠标不在滚动区域内，直接把 mouseY 设置为 -1000，防止按钮 hover
            mouseY = -1000;
        } else {
            // 轮盘按钮考虑偏移然后渲染
            mouseY += this.configScrollY;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, -this.configScrollY);
        for (Renderable renderable : ((ScreenAccessor) this).ysm$renderables()) {
            if (renderable instanceof IConfigFormsButton) {
                renderable.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
        }
        graphics.pose().popMatrix();
        RenderUtil.popScissor(graphics);

        this.drawTooltips(graphics, mouseX, mouseY);
    }

    private void drawTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (-1 < selectId && selectId < extraAnimationMap.size()) {
            String key = extraAnimationMap.getKeyAt(selectId);
            String descKey = "properties.extra_animation.%s.desc".formatted(key);
            String desc = LanguageManager.getI18n(this.model, descKey, StringUtils.EMPTY);
            if (StringUtils.isNotBlank(desc)) {
                List<FormattedCharSequence> split = font.split(Component.literal(desc), 240);
                graphics.setTooltipForNextFrame(font, split, mouseX, mouseY);
            }
        }
    }

    private void executeMolang(String molang, @Nullable Consumer<String> resultConsumer) {
        try {
            IValue parsed = CustomMolangParser.parseSingleExpressionUnsafe(molang);
            this.animatableEntity.executeMolangExp(parsed, true, false, resultConsumer);
        } catch (ParseException exception) {
            YesSteveModel.LOGGER.error(exception);
        }
    }

    private void drawPageText(GuiGraphicsExtractor graphics) {
        graphics.fill(this.x + 157, this.y - 87, this.x + 238, this.y - 72, 0xCF000000);
        String pageText = String.format("%d/%d", current.getRight() + 1, (this.extraAnimationMap.size() - 1) / MAX_ROULETTE_COUNT + 1);
        graphics.centeredText(font, pageText, this.x + 197, this.y - 83, 0xFF000000 | ChatFormatting.AQUA.getColor());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double scroll = scrollY;
        if (scroll < 0) {
            // 如果在屏幕的左半边是滚动轮盘，右半边是滚动配置
            if (mouseX < this.x + 110) {
                this.pageDown();
            } else {
                this.configScrollDown(20);
            }
            return true;
        }
        if (scroll > 0) {
            // 如果在屏幕的左半边是滚动轮盘，右半边是滚动配置
            if (mouseX < this.x + 110) {
                this.pageUp();
            } else {
                this.configScrollUp(20);
            }
            return true;
        }
        return false;
    }

    private void pageUp() {
        current.setValue(Math.max(0, current.getRight() - 1));
    }

    private void pageDown() {
        int page = (current.getRight() + 1) * MAX_ROULETTE_COUNT;
        if (this.extraAnimationMap.size() > page) {
            current.setValue(current.getRight() + 1);
        }
    }

    private void configScrollUp(int count) {
        this.configScrollY = Math.max(0, this.configScrollY - count);
    }

    private void configScrollDown(int count) {
        this.configScrollY = Math.min(this.maxScrollY, this.configScrollY + count);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (-1 < selectId && selectId < extraAnimationMap.size()) {
            // 点击普通界面
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            String selectKey = extraAnimationMap.getKeyAt(selectId);
            if (SPEC_RETURN.equals(selectKey)) {
                // 如果是 #return，那么就是自定义的返回按钮
                this.clickReturn();
            } else if (selectKey.startsWith(SPEC_PREFIX)) {
                // 如果 key 以 # 开头，那么说明选择的是子页面
                this.clickClassify(selectKey);
            } else {
                // 否则执行默认行为
                this.clickDefault(selectKey);
            }
        } else if (-1 < hoverConfigId && hoverConfigId < extraAnimationMap.size()) {
            // 点击配置按钮
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            // 以防万一，再检查一次
            String selectValue = extraAnimationMap.getValueAt(hoverConfigId);
            if (selectValue.startsWith(SPEC_PREFIX)) {
                selectValue = selectValue.substring(SPEC_PREFIX.length());
                if (this.buttonMap.containsKey(selectValue)) {
                    clickConfig(selectValue);
                }
            }
        }

        for (GuiEventListener listener : this.children()) {
            double mouseYOffset = mouseY;
            // 配置按钮需要考虑滚动偏移
            if (listener instanceof IConfigFormsButton) {
                mouseYOffset = mouseY + this.configScrollY;
            }
            if (listener.mouseClicked(new MouseButtonEvent(mouseX, mouseYOffset, new MouseButtonInfo(button, 0)), false)) {
                this.setFocused(listener);
                if (button == 0) {
                    this.setDragging(true);
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (AnimationRouletteKey.ANIMATION_ROULETTE_KEY.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private void clickConfig(String buttonName) {
        this.configButtons = this.buttonMap.get(buttonName);
        this.configScrollY = 0;
        this.maxScrollY = 0;
        this.init();
    }

    private void clickDefault(String selectKey) {
        LocalPlayer player = this.minecraft.player;
        // 乐观播放：本地立刻开始，不等服务端往返（否则要等状态同步回来才播，体感很慢）；
        // 服务端的权威状态稍后到达，内容一致
        playLocally(selectKey);
        if (NetworkHandler.isRemoteChannelPresent()) {
            var peekLast = CACHE.peekLast();
            String classifyId = "";
            if (peekLast != null && StringUtils.isNotBlank(peekLast.getLeft())) {
                classifyId = peekLast.getLeft();
            }
            Entity entity = animatableEntity.getEntity();
            if (entity instanceof Player) {
                ClientProtocolGateway.playSelfAnimation(selectKey);
            } else {
                ClientProtocolGateway.playMaidAnimation(entity.getId(), selectId, classifyId);
            }
        } else if (player != null) {
            EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(cap -> cap.playExtraAnimation(selectKey));
        }
        if (player != null && ClientConfig.PRINT_ANIMATION_ROULETTE_MSG.get()) {
            MutableComponent component = Component.translatable("message.yes_steve_model.model.animation_roulette.play", selectKey);
            player.sendSystemMessage(component);
        }
        this.minecraft.setScreen(null);
    }

    /** 本地立即播放（仅本地玩家实体），服务端同步到达后一致 */
    private void playLocally(String animationName) {
        if (this.animatableEntity instanceof com.elfmcys.ysm.client.entity.CustomPlayerEntity playerEntity
                && playerEntity.getEntity() == this.minecraft.player) {
            playerEntity.playExtraAnimation(animationName);
        }
    }

    private void clickClassify(String selectKey) {
        // 最多让你套 5 层
        if (CACHE.size() > 5) {
            LocalPlayer player = this.minecraft.player;
            if (player != null) {
                player.sendSystemMessage(Component.translatable("gui.yes_steve_model.roulette.too_long"));
            }
            return;
        }
        String key = selectKey.substring(SPEC_PREFIX.length());
        FifoHashMap<String, String> map = classifyMap.get(key);
        if (map != null) {
            CACHE.addLast(MutablePair.of(key, 0));
            AnimationRouletteScreen screen = new AnimationRouletteScreen(this.buttonMap, this.classifyMap, this.model, this.animatableEntity);
            this.minecraft.setScreen(screen);
        }
    }

    private void clickReturn() {
        if (CACHE.size() > 1) {
            CACHE.removeLast();
            AnimationRouletteScreen screen = new AnimationRouletteScreen(this.buttonMap, this.classifyMap, this.model, this.animatableEntity);
            this.minecraft.setScreen(screen);
        } else {
            this.minecraft.setScreen(null);
        }
    }

    public static void addRootClassify(String keyName) {
        CACHE.clear();
        CACHE.addLast(MutablePair.of("", 0));
        CACHE.addLast(MutablePair.of(keyName, 0));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawRouletteText(GuiGraphicsExtractor graphics) {
        float startDeg = Mth.PI / MAX_ROULETTE_COUNT;
        int remainSize = extraAnimationMap.size() - current.getRight() * MAX_ROULETTE_COUNT;
        int radius = 65;

        for (int i = 0; i < Math.min(MAX_ROULETTE_COUNT, remainSize); i++) {
            int index = i + current.getRight() * 8;
            int xPos = (int) (x + radius * Mth.cos(startDeg));
            int yPos = (int) (y + radius * Mth.sin(startDeg) - font.lineHeight / 2f);
            String animationValue = extraAnimationMap.getValueAt(index);

            boolean isClassify = extraAnimationMap.getKeyAt(index).startsWith(SPEC_PREFIX);
            boolean hasConfig = animationValue.startsWith(SPEC_PREFIX);

            // 如果含有配置
            if (hasConfig) {
                String configValue = animationValue.substring(SPEC_PREFIX.length());
                if (buttonMap.containsKey(configValue)) {
                    ExtraAnimationButton button = buttonMap.get(configValue);
                    animationValue = button.getName();

                    int configR = 35;
                    int configIconX = (int) (x + configR * Mth.cos(startDeg));
                    int configIconY = (int) (y + configR * Mth.sin(startDeg) - font.lineHeight / 2f);
                    graphics.centeredText(font, Component.literal("⚙").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), configIconX, configIconY, 0xFFFFFFFF);
                }
            }

            // 绘制文本
            if (StringUtils.isNoneBlank(animationValue)) {
                String key = LanguageManager.getI18n(this.model, "properties.extra_animation.%s".formatted(extraAnimationMap.getKeyAt(index)), animationValue);
                if (i < 3) {
                }
                MutableComponent name = Component.literal(key);
                this.drawAnimationName(graphics, name, xPos, yPos, isClassify);
            } else {
                String key = LanguageManager.getI18n(this.model, "properties.extra_animation.%s".formatted(extraAnimationMap.getKeyAt(index)), String.valueOf(index));
                if (i < 3) {
                }
                MutableComponent name = Component.literal(key);
                graphics.centeredText(font, name, xPos, yPos - 8, 0xFFF3EFE0);
            }
            // 只有第 0 页显示按键绑定
            // 如果当前是分类菜单，也不显示按键绑定
            if (current.getRight() == 0 && CACHE.size() == 1) {
                this.drawKeyMappingName(graphics, index, xPos, yPos);
            }
            startDeg = startDeg + 2 * Mth.PI / MAX_ROULETTE_COUNT;
        }
    }

    private void drawKeyMappingName(GuiGraphicsExtractor graphics, int index, int xPos, int yPos) {
        MutableComponent keyText = Component.literal("[ ").withStyle(ChatFormatting.YELLOW);
        KeyMapping keyMapping = ExtraAnimationKey.EXTRA_ANIMATION_KEYS.get(index);
        if (keyMapping.isUnbound()) {
            keyText.append(Component.translatable("key.yes_steve_model.extra_animation.none"));
        } else {
            keyText.append(keyMapping.getTranslatedKeyMessage());
        }
        keyText.append(" ]");
        graphics.centeredText(font, keyText, xPos, yPos + 4, 0xFFF3EFE0);
    }

    private void drawAnimationName(GuiGraphicsExtractor graphics, MutableComponent name, int xPos, int yPos, boolean isClassify) {
        int maxLength = 50;
        int lineHeight = font.lineHeight;

        if (isClassify) {
            name = name.withStyle(ChatFormatting.RED);
        }
        var split = font.split(name, maxLength);
        int yoffset = yPos - split.size() * lineHeight + 2;
        // 只有第一页的轮盘能够绑定键位，其他的不行
        // 分类菜单也应该不显示按键
        if (current.getRight() != 0 || CACHE.size() > 1) {
            // 应该下移两行
            yoffset = yoffset + lineHeight;
        }
        for (FormattedCharSequence sequence : split) {
            graphics.centeredText(font, sequence, xPos, yoffset, 0xFFF3EFE0);
            yoffset = yoffset + lineHeight;
        }
    }

    private void drawRouletteBg(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // extraAnimationMap 可能为空
        if (this.extraAnimationMap.isEmpty()) {
            return;
        }

        List<FanSlice> slices = new ObjectArrayList<>();
        int count = 8;        float theta = (float) Mth.atan2(mouseY - y, mouseX - x);
        if (theta < 0) {
            theta = Mth.PI * 2 + theta;
        }
        float distance = Mth.sqrt(Mth.square(mouseY - y) + Mth.square(mouseX - x));
        boolean isSelected = false;
        boolean isConfigSelected = false;

        for (int i = 0; i < Math.min(8, extraAnimationMap.size() - current.getRight() * 8); i++) {
            float spacingDeg = Mth.PI / 90;
            float startDeg = (2 * Mth.PI / count) * i + spacingDeg;
            float endDeg = (2 * Mth.PI / count) * (i + 1) - spacingDeg;
            int index = i + current.getRight() * 8;
            boolean hasConfig = extraAnimationMap.getValueAt(index).startsWith(SPEC_PREFIX);

            isSelected = onDrawFan(slices, startDeg, theta, endDeg, distance, isSelected, hasConfig, i);

            boolean isConfigHover = startDeg < theta && theta < endDeg && 20 < distance && distance < 50;
            if (hasConfig) {
                if (isConfigHover) {
                    slices.add(new FanSlice(15, 50, startDeg, endDeg, 0xf000ceff));
                    isConfigSelected = true;
                    this.hoverConfigId = index;
                } else {
                    slices.add(new FanSlice(25, 50, startDeg, endDeg, 0x7000ceff));
                }
            }
        }
        if (!isSelected) {
            this.selectId = -1;
        }
        if (!isConfigSelected) {
            this.hoverConfigId = -1;
        }

        // 26.1.2：立即模式的 Tesselator 已被移除，改为提交自定义 GUI 元素（参考官方 2.6.5）
        ScreenRectangle scissor = RenderUtil.peekScissor(graphics);
        ScreenRectangle bounds = new ScreenRectangle(this.x, this.y, this.width, this.height);        RenderUtil.submitGuiElement(graphics, new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer consumer) {
                for (FanSlice slice : slices) {
                    float alpha = (slice.color() >> 24 & 255) / 255.0F;
                    float red = (slice.color() >> 16 & 255) / 255.0F;
                    float green = (slice.color() >> 8 & 255) / 255.0F;
                    float blue = (slice.color() & 255) / 255.0F;
                    consumer.addVertex(x + slice.rOut() * Mth.cos(slice.startDeg()), y + slice.rOut() * Mth.sin(slice.startDeg()), 0).setColor(red, green, blue, alpha);
                    consumer.addVertex(x + slice.rIn() * Mth.cos(slice.startDeg()), y + slice.rIn() * Mth.sin(slice.startDeg()), 0).setColor(red, green, blue, alpha);
                    consumer.addVertex(x + slice.rIn() * Mth.cos(slice.endDeg()), y + slice.rIn() * Mth.sin(slice.endDeg()), 0).setColor(red, green, blue, alpha);
                    consumer.addVertex(x + slice.rOut() * Mth.cos(slice.endDeg()), y + slice.rOut() * Mth.sin(slice.endDeg()), 0).setColor(red, green, blue, alpha);
                }
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Nullable
            @Override
            public ScreenRectangle scissorArea() {
                return scissor;
            }

            @Override
            public ScreenRectangle bounds() {
                return bounds;
            }
        });
    }

    private boolean onDrawFan(List<FanSlice> slices, float startDeg, float theta, float endDeg, float distance, boolean isSelected, boolean hasConfig, int i) {
        boolean hovered = startDeg < theta && theta < endDeg && 50 < distance && distance < 100;
        if (hovered) {
            isSelected = true;
            this.selectId = i + current.getRight() * 8;
        }
        if (hovered && i < extraAnimationMap.size()) {
            if (hasConfig) {
                slices.add(new FanSlice(50, 115, startDeg, endDeg, 0xf0FFB100));
                slices.add(new FanSlice(25, 50, startDeg, endDeg, 0x90000000));
            } else {
                slices.add(new FanSlice(25, 115, startDeg, endDeg, 0xf0FFB100));
            }
        } else {
            slices.add(new FanSlice(25, 105, startDeg, endDeg, 0x90000000));
        }

        return isSelected;
    }

    private record FanSlice(float rIn, float rOut, float startDeg, float endDeg, int color) {
    }
}
