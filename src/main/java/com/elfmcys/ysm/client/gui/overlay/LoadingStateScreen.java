package com.elfmcys.ysm.client.gui.overlay;

import com.elfmcys.ysm.client.model.ClientModelService;
import com.elfmcys.ysm.config.LoadingStateScreenConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class LoadingStateScreen implements HudElement {
    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (LoadingStateScreenConfig.DISABLE_LOADING_STATE_SCREEN.get()) {
            return;
        }

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        // 根据配置决定位置
        LoadingStateScreenConfig.Position position = LoadingStateScreenConfig.LOADING_STATE_POSITION.get();
        int x, y, barX, barY;
        // 渲染一个 150 长度的进度条
        int barWidth = 150;
        int barHeight = 10;
        switch (position) {
            case TOP_LEFT -> {
                x = 10;
                y = 10;
                barX = 10;
                barY = 22;
            }
            case TOP_CENTER -> {
                x = screenWidth / 2;
                y = 10;
                barX = (screenWidth - barWidth) / 2;
                barY = 22;
            }
            case TOP_RIGHT -> {
                x = screenWidth - 10;
                y = 10;
                barX = screenWidth - 10 - barWidth;
                barY = 22;
            }
            case BOTTOM_LEFT -> {
                x = 10;
                y = screenHeight - 30;
                barX = 10;
                barY = screenHeight - 8 - barHeight;
            }
            case BOTTOM_CENTER -> {
                x = screenWidth / 2;
                y = screenHeight - 85;
                barX = (screenWidth - barWidth) / 2;
                barY = screenHeight - 63 - barHeight;
            }
            case BOTTOM_RIGHT -> {
                x = screenWidth - 10;
                y = screenHeight - 30;
                barX = screenWidth - 10 - barWidth;
                barY = screenHeight - 8 - barHeight;
            }
            default -> {
                x = screenWidth / 2;
                y = 10;
                barX = (screenWidth - barWidth) / 2;
                barY = 22;
            }
        }

        var service = ClientModelService.current().orElse(null);
        if (service == null) {
            return;
        }
        var loading = service.loadingCount();
        if (loading > 0) {
            MutableComponent text = Component.translatable("gui.yes_steve_model.sync_hint.title")
                    .append(Component.translatable("gui.yes_steve_model.sync_hint.loading_models", loading,
                            service.catalog().models().size()).withStyle(ChatFormatting.YELLOW));
            this.drawStringAtPosition(guiGraphics, text, x, y, screenWidth);
        }
    }

    private void drawStringAtPosition(GuiGraphicsExtractor guiGraphics, MutableComponent text, int x, int y, int screenWidth) {
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int drawX = switch (LoadingStateScreenConfig.LOADING_STATE_POSITION.get()) {
            case TOP_LEFT, BOTTOM_LEFT -> x;
            case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - textWidth) / 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> x - textWidth;
        };
        guiGraphics.text(font, text, drawX, y, 0xFFFFFF);
    }
}
