package com.elfmcys.ysm.client.compat.realcamera;

/**
 * RealCamera 在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class RealCameraCompat {
    public static void init() {
    }

    public static boolean isActive() {
        return false;
    }
}
