package com.elfmcys.ysm.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FlatRatioBox extends AbstractWidget implements IConfigFormsButton {
    private final int maxHeight;

    public FlatRatioBox(int pX, int pY, int maxHeight, Component component) {
        super(pX, pY, 115, 15, component);
        this.maxHeight = maxHeight;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + maxHeight, 0xef_434242);
        this.extractScrollingStringOverContents(graphics.textRenderer(), this.getMessage(), 2);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
