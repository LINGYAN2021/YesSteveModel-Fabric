package com.elfmcys.ysm.format.parser;

import com.elfmcys.ysm.buffer.NativeBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Ogg 容器音频的元数据探针：只读头部（格式、声道、采样率）和文件尾
 * （最后一个 OggS 页的 granulepos 得出总采样数），不做完整解码。
 */
public final class SoundProbe {
    private static final byte[] VORBIS_MAGIC = {0x01, 'v', 'o', 'r', 'b', 'i', 's'};
    private static final byte[] OPUS_MAGIC = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd'};
    private static final byte[] PAGE_MAGIC = {'O', 'g', 'g', 'S'};
    private static final int HEAD_BYTES = 8192;
    private static final int TAIL_BYTES = 65536;

    public record Info(String encoding, int channels, int sampleRate, long samples) {
    }

    public static Info probe(NativeBuffer buffer) throws IOException {
        var data = buffer.nio().order(ByteOrder.LITTLE_ENDIAN);
        int size = data.remaining();

        var headLen = Math.min(HEAD_BYTES, size);
        int vorbis = indexOf(data, 0, headLen, VORBIS_MAGIC);
        int opus = vorbis >= 0 ? -1 : indexOf(data, 0, headLen, OPUS_MAGIC);
        if (vorbis < 0 && opus < 0) {
            throw new IOException("Not an Ogg Vorbis/Opus stream");
        }

        final String encoding;
        final int channels;
        final int sampleRate;
        final int preSkip;
        if (vorbis >= 0) {
            encoding = "OGG_VORBIS";
            channels = Byte.toUnsignedInt(data.get(vorbis + 11));
            sampleRate = data.getInt(vorbis + 12);
            preSkip = 0;
        } else {
            encoding = "OGG_OPUS";
            channels = Byte.toUnsignedInt(data.get(opus + 9));
            preSkip = Short.toUnsignedInt(data.getShort(opus + 10));
            sampleRate = data.getInt(opus + 12);
        }

        long granule = lastGranulePos(data, size);
        long samples = Math.max(0L, granule - preSkip);
        return new Info(encoding, channels, sampleRate, samples);
    }

    /**
     * 最后一个 Ogg 页的 granulepos（总 PCM 采样数，opus 为 48kHz 单位）。
     * 从文件尾往前找最后一个 "OggS" 捕获标记。
     */
    private static long lastGranulePos(ByteBuffer data, int size) throws IOException {
        int from = Math.max(0, size - TAIL_BYTES);
        int found = -1;
        int pos = from;
        while (true) {
            int next = indexOf(data, pos, size, PAGE_MAGIC);
            if (next < 0) {
                break;
            }
            found = next;
            pos = next + PAGE_MAGIC.length;
        }
        if (found < 0 || found + 14 > size) {
            throw new IOException("No Ogg page found");
        }
        return data.getLong(found + 6);
    }

    private static int indexOf(ByteBuffer data, int from, int to, byte[] magic) {
        outer:
        for (int i = from; i + magic.length <= to; i++) {
            for (int j = 0; j < magic.length; j++) {
                if (data.get(i + j) != magic[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private SoundProbe() {
    }
}
