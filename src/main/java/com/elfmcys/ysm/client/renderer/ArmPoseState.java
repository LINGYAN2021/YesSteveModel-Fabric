package com.elfmcys.ysm.client.renderer;

/**
 * 第一人称手臂摆放参数，可在游戏内用 {@code /ysmclient armpose} 实时调整。
 *
 * <p>默认值照搬官方 1.21.1 实现（官方 2.6.x fabric 渲染类字节码）：在相机空间姿态栈上
 * 只做 {@code translate(0.0, -1.5, 0.0)}，不旋转、不缩放，整条手臂模型画一次。
 * 绑定姿态的手臂柱在相机两侧形成自然的透视缩短效果，即官方第一人称的样子。
 *
 * <p>注意：官方第一人称不乘模型的 widthScale/heightScale（直调 NativeRenderer.render，
 * 不走第三人称 preRender 的缩放），渲染器里也不要加回来。
 *
 * <p>官方 26.1 neoforge 改成了每只手 {@code translate(±0.25, 1.8, 0)} + {@code scale(-1,-1,1)}，
 * 可用 {@code /ysmclient armpose y 1.8} + {@code sx -1} + {@code sy -1} 复现。
 */
public final class ArmPoseState {
    private static volatile float x = 0.0F;
    private static volatile float y = -1.5F;
    private static volatile float z = 0.0F;
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
