package com.elfmcys.ysm.client.sound.instance;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;

import java.util.concurrent.CompletableFuture;

/**
 * 26.1.2 的 SoundInstance 不再有 getStream，改由 SoundEngineMixin 识别此接口并转发
 */
public interface ICustomStreamSound {
    CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping);
}
