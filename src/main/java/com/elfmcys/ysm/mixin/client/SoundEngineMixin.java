package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.sound.instance.ICustomStreamSound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Redirect(method = "play", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/sounds/SoundBufferLibrary;getStream(Lnet/minecraft/resources/Identifier;Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<AudioStream> ysm$customStream(SoundBufferLibrary soundBuffers, Identifier path,
                                                            boolean looping, SoundInstance instance) {
        if (instance instanceof ICustomStreamSound custom) {
            return custom.getStream(soundBuffers, instance.getSound(), looping);
        }
        return soundBuffers.getStream(path, looping);
    }
}
