package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.sound.instance.ICustomStreamSound;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

/**
 * 不用 sponge 原生 @Redirect：javac 会把单元素 at 编译成数组形态，
 * mixinextras 的 FactoryRedirectWrapperMixinTransformer 扫描 @Redirect 时
 * 对数组形态直接 ClassCastException，故改用 mixinextras 的 @WrapOperation。
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @WrapOperation(method = "play", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/sounds/SoundBufferLibrary;getStream(Lnet/minecraft/resources/Identifier;Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<AudioStream> ysm$customStream(SoundBufferLibrary soundBuffers, Identifier path,
                                                            boolean looping,
                                                            Operation<CompletableFuture<AudioStream>> original,
                                                            SoundInstance instance) {
        if (instance instanceof ICustomStreamSound custom) {
            return custom.getStream(soundBuffers, instance.getSound(), looping);
        }
        return original.call(soundBuffers, path, looping);
    }
}
