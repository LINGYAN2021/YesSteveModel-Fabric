package com.elfmcys.ysm.client.renderer;

/**
 * 第一人称手臂摆放参数，可在游戏内用 {@code /ysmclient armpose} 实时调整。
 *
 * <p>默认值：X/Y/Z 取自官方实现里唯一那句 {@code translate(0.0, -1.5, 0.0)}；
 * yaw 传 0 让渲染器的 {@code (180 - yaw)} 翻转 180°——手臂模型的动画姿态是朝 +Z
 * 伸出的，而相机看向 -Z，不翻转时手臂会伸到相机身后被近裁剪面裁掉。
 *
 * <p>之所以做成可调，是因为这些数值只能靠目视确定，而调整方看得见画面。
 */
public final class ArmPoseState {
    private static volatile float x = 0.0F;
    private static volatile float y = -1.5F;
    private static volatile float z = 0.0F;
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
            case "yaw" -> yaw = value;
            case "pitch" -> pitch = value;
            default -> {
                return false;
            }
        }
        return true;
    }

    public static String describe() {
        return String.format("x=%.3f y=%.3f z=%.3f yaw=%.1f pitch=%.1f", x, y, z, yaw, pitch);
    }
}
