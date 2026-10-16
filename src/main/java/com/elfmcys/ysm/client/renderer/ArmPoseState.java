package com.elfmcys.ysm.client.renderer;

/**
 * 第一人称手臂摆放的目视微调参数，可在游戏内用 {@code /ysmclient armpose} 实时调整。
 *
 * <p>x/y/z 是<b>相机空间</b>的整体偏移（应用在原版变换链之前）：y+ 抬高手臂、
 * x+ 往右、z+ 拉近/拉远（z 负为向前）。yaw/pitch 是链条前的相机空间旋转。
 * sx/sy/sz 是模型空间缩放，默认 (1,-1,-1)（绕 X 180°，把手臂 -y 延伸方向
 * 对齐到原版部件的 +y），正常不需要动。
 *
 * <p>肩关节对齐偏移（±0.072, 1.861, 0）是固定常量，在渲染器里。
 */
public final class ArmPoseState {
    private static volatile float x = 0.0F;
    private static volatile float y = 0.15F;
    private static volatile float z = 0.0F;
    private static volatile float sx = 1.0F;
    private static volatile float sy = -1.0F;
    private static volatile float sz = -1.0F;
    private static volatile float yaw = 0.0F;
    private static volatile float pitch = 0.0F;

    private ArmPoseState() {
    }

    public static float x() {
        return x;
    }

    public static float y() {
        return y;
    }

    public static float z() {
        return z;
    }

    public static float sx() {
        return sx;
    }

    public static float sy() {
        return sy;
    }

    public static float sz() {
        return sz;
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }

    /**
     * 按轴名设置，未知轴名返回 false
     */
    public static boolean set(String axis, float value) {
        switch (axis) {
            case "x" -> x = value;
            case "y" -> y = value;
            case "z" -> z = value;
            case "sx" -> sx = value;
            case "sy" -> sy = value;
            case "sz" -> sz = value;
            case "yaw" -> yaw = value;
            case "pitch" -> pitch = value;
            default -> {
                return false;
            }
        }
        return true;
    }

    public static String describe() {
        return String.format("x=%.3f y=%.3f z=%.3f sx=%.1f sy=%.1f sz=%.1f yaw=%.1f pitch=%.1f",
                x, y, z, sx, sy, sz, yaw, pitch);
    }
}
