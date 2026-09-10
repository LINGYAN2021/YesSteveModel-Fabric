package com.elfmcys.ysm.client.compat.touhoulittlemaid;

/**
 * 兼容层桩：联动模组在 Fabric 26.1.2 上不存在，全部返回安全默认值。
 * 保留原公开签名以便后续重新对接。
 */
public class TlmCommonCompat {
    public static boolean isInstalled() {
        return false;
    }

    public static void registerEvent() {
    }

    public static boolean isMaid(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    public static void onProjectileSetOwner(net.minecraft.world.entity.projectile.Projectile projectile,
            net.minecraft.world.entity.Entity entity) {
    }

    public static void setRouletteAnim(net.minecraft.world.entity.Entity entity, String classifyId, int extraAnimIndex) {
    }

    public static boolean canControlMaid(net.minecraft.world.entity.Entity entity,
            net.minecraft.server.level.ServerPlayer player) {
        return false;
    }

    public static void handleExecuteMolang(net.minecraft.world.entity.Entity entity, String molangExpression) {
    }
}
