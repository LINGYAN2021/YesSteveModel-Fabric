package com.elfmcys.ysm.client.renderer;

/**
 * 第一人称手臂摆放参数，可在游戏内用 {@code /ysmclient armpose} 实时调整。
 *
 * <p>渲染器先照搬原版 {@code renderPlayerArm} 空手分支的变换链（把模型放到原版手臂的位置），
 * 这里的 x/y/z/sx/sy/sz 是链条之后、模型空间里的对齐偏移：把手臂模型的肩关节
 * （约 (±0.072, 1.861, 0)，方块单位）对齐到原版手臂部件原点，并用
 * {@code scale(1,-1,-1)}（绕 X 180°）把手臂的 -y 延伸方向对齐到原版部件的 +y。
 * yaw/pitch 在缩放前绕 Y/X 旋转，供目视微调。
 *
 * <p>注意：官方第一人称不乘模型的 widthScale/heightScale（直调 NativeRenderer.render，
 * 不走第三人称 preRender 的缩放），渲染器里也不要加回来。
 */
public final class ArmPoseState {
    private static volatile float x = -0.072F;
    private static volatile float y = 1.861F;
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
