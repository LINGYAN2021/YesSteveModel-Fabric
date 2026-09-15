package com.elfmcys.ysm.client.compat.swem;

import com.elfmcys.ysm.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;

/**
 * SWEM 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class SwemCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static String getAnimation(LivingEntity livingEntity) {
        return "";
    }

    public static void addBinding(CtrlBinding binding) {
    }
}
