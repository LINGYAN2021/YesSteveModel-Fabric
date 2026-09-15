package com.elfmcys.ysm.client.gui.button;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.StarModelsCapabilityProvider;
import com.elfmcys.ysm.network.fabric.ClientProtocolGateway;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import net.minecraft.client.renderer.RenderPipelines;

public class StarButton extends FlatColorButton {
    private final static Identifier ICON = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/icon.png");

    public StarButton(int x, int y) {
        super(x, y, 20, 20, Component.empty(), (b) -> {
        });
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float pPartialTick) {
        super.extractContents(graphics, mouseX, mouseY, pPartialTick);
        int startX = (this.width - 16) / 2;
        int startY = (this.height - 16) / 2;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(modelInfoCap -> EntityCapabilityHolder.get(player, StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(starModelsCap -> {
                var modelHash = modelInfoCap.getModelHash();
                if (modelHash != null && starModelsCap.containModel(modelHash)) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, ICON, this.getX() + startX, this.getY() + startY, 16, 16, 16, 0, 16, 16, 256, 256);
                } else {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, ICON, this.getX() + startX, this.getY() + startY, 16, 16, 0, 0, 16, 16, 256, 256);
                }
            }));
        }
    }

    @Override
    public void onPress(net.minecraft.client.input.InputWithModifiers input) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(modelInfoCap -> EntityCapabilityHolder.get(player, StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(starModelsCap -> {
                var modelHash = modelInfoCap.getModelHash();
                if (modelHash == null) {
                    return;
                }
                if (starModelsCap.containModel(modelHash)) {
                    starModelsCap.removeModel(modelHash);
                    ClientProtocolGateway.updateStar(modelHash, false);
                } else {
                    starModelsCap.addModel(modelHash);
                    ClientProtocolGateway.updateStar(modelHash, true);
                }
            }));
        }
    }
}
