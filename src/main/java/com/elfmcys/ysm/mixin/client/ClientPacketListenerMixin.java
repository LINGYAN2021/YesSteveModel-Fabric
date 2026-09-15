package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.event.LocalPlayerRespawnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Unique
    private LocalPlayer ysm$oldPlayer;

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void ysm$captureOldPlayer(ClientboundRespawnPacket packet, CallbackInfo ci) {
        ysm$oldPlayer = Minecraft.getInstance().player;
    }

    @Inject(method = "handleRespawn", at = @At("RETURN"))
    private void ysm$fireClone(ClientboundRespawnPacket packet, CallbackInfo ci) {
        LocalPlayer oldPlayer = ysm$oldPlayer;
        LocalPlayer newPlayer = Minecraft.getInstance().player;
        ysm$oldPlayer = null;
        if (oldPlayer != null && newPlayer != null && oldPlayer != newPlayer) {
            LocalPlayerRespawnEvent.onClone(oldPlayer, newPlayer);
        }
    }
}
