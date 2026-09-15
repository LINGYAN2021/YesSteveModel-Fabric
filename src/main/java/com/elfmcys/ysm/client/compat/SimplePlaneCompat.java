package com.elfmcys.ysm.client.compat;

import com.elfmcys.ysm.client.entity.CustomVehicleEntity;
import com.elfmcys.ysm.geckolib3.core.event.predicate.AnimationEvent;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * 兼容模组在 Fabric 26.1.2 上不存在，保留空实现以维持调用点结构。
 */
public class SimplePlaneCompat {
    public static void init() {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static Optional<Vector3f> getRotation(AnimationEvent<CustomVehicleEntity> event) {
        return Optional.empty();
    }
}
