package com.elfmcys.ysm.event.api;

import com.elfmcys.ysm.client.entity.CustomPlayerEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import com.elfmcys.ysm.event.bus.YsmCancelable;
import com.elfmcys.ysm.event.bus.YsmEvent;
import org.jetbrains.annotations.Nullable;

@YsmCancelable
public class SpecialPlayerRenderEvent extends YsmEvent {
    private final Player player;
    private final CustomPlayerEntity customPlayer;
    private final String modelId;
    @Nullable
    private Identifier textureLocationOverride;

    // 没这个方法 forge 会报错
    public SpecialPlayerRenderEvent() {
        player = null;
        customPlayer = null;
        modelId = null;
    }

    public SpecialPlayerRenderEvent(Player player, CustomPlayerEntity customPlayer, String modelId) {
        this.player = player;
        this.customPlayer = customPlayer;
        this.modelId = modelId;
    }

    public Player getPlayer() {
        return player;
    }

    public CustomPlayerEntity getCustomPlayer() {
        return customPlayer;
    }

    public String getModelId() {
        return modelId;
    }

    @Nullable
    public Identifier getTextureLocationOverride() {
        return textureLocationOverride;
    }

    public void setTextureLocationOverride(@Nullable Identifier textureLocationOverride) {
        this.textureLocationOverride = textureLocationOverride;
    }
}
