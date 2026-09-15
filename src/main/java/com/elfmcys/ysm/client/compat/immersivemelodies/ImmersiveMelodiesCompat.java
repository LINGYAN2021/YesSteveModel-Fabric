package com.elfmcys.ysm.client.compat.immersivemelodies;

import com.elfmcys.ysm.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;

/**
 * Immersive Melodies 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class ImmersiveMelodiesCompat {
    public static void init() {
    }

    public static void updateMelodyProgress(LivingEntity entity, ImmersiveMelodiesData imData) {
    }

    public static void addBinding(CtrlBinding binding) {
    }

    public static final class ImmersiveMelodiesData {
        public float pitch = 0f;
        public float volume = 0f;
        public float current = 0f;
        public long delta = 0L;
        public long time = 0L;
    }
}
