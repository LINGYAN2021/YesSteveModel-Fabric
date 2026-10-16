package com.elfmcys.ysm.client.renderer;

/**
 * 第一人称手臂摆放参数，可在游戏内用 {@code /ysmclient armpose} 实时调整。
 *
 * <p>默认值是进游戏目视调出来的：相机空间姿态栈上 {@code translate(0.35, -2.2, -0.45)}，
 * 不旋转、不缩放，整条手臂模型画一次——手臂整体在相机前下方偏右，
 * 从上方俯视前臂与手背，接近官方第一人称的样子。
 *
 * <p>注意：官方第一人称不乘模型的 widthScale/heightScale（直调 NativeRenderer.render，
 * 不走第三人称 preRender 的缩放），渲染器里也不要加回来。
 *
 * <p>参考值：官方 1.21.1 为 {@code translate(0, -1.5, 0)}；官方 26.1 neoforge 为每只手
 * {@code translate(±0.25, 1.8, 0)} + {@code scale(-1,-1,1)}（可用
 * {@code /ysmclient armpose y 1.8} + {@code sx -1} + {@code sy -1} 复现）。
 */
public final class ArmPoseState {
    private static volatile float x = 0.35F;
    private static volatile float y = -2.2F;
    private static volatile float z = -0.45F;
    private static volatile float sx = 1.0F;
    private static volatile float sy = 1.0F;
    private static volatile float sz = 1.0F;
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
