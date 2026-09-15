package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.YesSteveModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class FlatCheckbox extends AbstractWidget implements IConfigFormsButton {
    private static final Identifier BUTTON_TEXTURE = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/roulette.png");
    private final Consumer<Boolean> onClick;
    private final Component name;
    protected boolean isStateTriggered;

    public FlatCheckbox(int xIn, int yIn, int width, Component name, Consumer<Boolean> onClick) {
        super(xIn, yIn, width, 12, name);
        this.name = name;
        this.onClick = onClick;
        this.isStateTriggered = false;
    }

    public FlatCheckbox(int xIn, int yIn, Component name, Consumer<Boolean> onClick) {
        this(xIn, yIn, 115, name, onClick);
    }

    public boolean isStateTriggered() {
        return isStateTriggered;
    }

    public void setStateTriggered(boolean stateTriggered) {
        this.isStateTriggered = stateTriggered;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int v = isStateTriggered ? 12 : 0;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, this.getX(), this.getY(),
                0, v, this.width, this.height, 256, 256);
        graphics.text(Minecraft.getInstance().font, name, this.getX() + 14, this.getY() + 2, 0xffffffff, false);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.isStateTriggered = !this.isStateTriggered;
        onClick.accept(this.isStateTriggered);
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
