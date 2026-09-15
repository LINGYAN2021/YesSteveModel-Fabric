package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.animation.molang.CustomMolangParser;
import com.elfmcys.ysm.config.ServerConfig;
import com.elfmcys.ysm.geckolib3.core.molang.value.IValue;
import com.elfmcys.ysm.geckolib3.model.AnimatableEntity;
import com.elfmcys.ysm.molang.parser.ParseException;
import com.elfmcys.ysm.network.NetworkHandler;
import com.elfmcys.ysm.network.fabric.ClientProtocolGateway;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.text.DecimalFormat;

/**
 * 26.1.2 移植：ForgeSlider 在 Fabric 上不存在，改为继承原版
 * {@link AbstractSliderButton}，自行处理 min/max/step 映射。
 */
public class FlatSlider extends AbstractSliderButton implements IConfigFormsButton {
    private static final Identifier BUTTON_TEXTURE = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/roulette.png");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private final AnimatableEntity<?> animatableEntity;
    private final String molang;
    private final Component prefix;
    private final double min;
    private final double max;
    private final double step;

    public FlatSlider(int x, int y, Component prefix, double currentValue, AnimatableEntity<?> animatableEntity, String molang,
                      double step, double min, double max) {
        super(x, y, 115, 15, prefix, Mth.clamp((currentValue - min) / (max - min), 0, 1));
        this.prefix = prefix;
        this.animatableEntity = animatableEntity;
        this.molang = molang;
        this.min = min;
        this.max = max;
        this.step = step;
        updateMessage();
    }

    public double getValue() {
        return snapToStep(this.value * (this.max - this.min) + this.min);
    }

    private double snapToStep(double val) {
        if (this.step > 0) {
            val = Math.round(val / this.step) * this.step;
        }
        return Mth.clamp(val, this.min, this.max);
    }

    public String getValueString() {
        return DECIMAL_FORMAT.format(this.getValue());
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.empty().append(this.prefix).append(": " + this.getValueString()));
    }

    @Override
    protected void applyValue() {
        // 把吸附后的值写回 normalized value
        this.value = (this.getValue() - this.min) / (this.max - this.min);
        try {
            String molangExpress = molang + "=" + getValue();
            IValue parsed = CustomMolangParser.parseSingleExpressionUnsafe(molangExpress);
            this.animatableEntity.executeMolangExp(parsed, true, false, null);
            if (!CustomMolangParser.hasOnlyRoamingAssignment(molangExpress) && NetworkHandler.isRemoteChannelPresent() && !ServerConfig.LOW_BANDWIDTH_USAGE.get()) {
                // 同步到周围的玩家
                ClientProtocolGateway.submitRouletteExpression(this.animatableEntity.getEntity(), molangExpress);
            }
        } catch (ParseException exception) {
            YesSteveModel.LOGGER.error(exception);
        }
        updateMessage();
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // roulette.png 中 y=24 为普通滑条背景，y=44 为悬停背景
        int v = 24 + (this.isHoveredOrFocused() ? 20 : 0);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, this.getX(), this.getY(), 0, v, this.width, this.height, 256, 256);
        int handleX = this.getX() + (int) (this.value * (double) (this.width - 8));
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, handleX, this.getY(), 0, v, 8, this.height, 256, 256);
        extractScrollingStringOverContents(
                guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE),
                this.getMessage(), 2);
    }
}
