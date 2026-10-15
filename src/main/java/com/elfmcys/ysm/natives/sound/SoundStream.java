package com.elfmcys.ysm.natives.sound;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 模型容器里的一段音频流。
 *
 * <p>由 {@code CommonAssetView.openSoundStream} 从容器分块中读出，
 * 上层再把 {@link #input()} 读完转成客户端的 {@code SoundData}。
 */
public class SoundStream implements Closeable {
    private final InputStream input;
    private final SoundFormat format;
    private final int sampleRate;
    private final long samples;
    private final int channels;

    public SoundStream(InputStream input, SoundFormat format, int sampleRate, long samples, int channels) {
        this.input = input;
        this.format = format;
        this.sampleRate = sampleRate;
        this.samples = samples;
        this.channels = channels;
    }

    public InputStream input() {
        return input;
    }

    public SoundFormat format() {
        return format;
    }

    public int sampleRate() {
        return sampleRate;
    }

    public long samples() {
        return samples;
    }

    public int channels() {
        return channels;
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
