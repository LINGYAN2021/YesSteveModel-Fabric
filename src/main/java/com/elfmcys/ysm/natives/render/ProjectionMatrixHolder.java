package com.elfmcys.ysm.natives.render;

import org.joml.Matrix4f;

/**
 * 由 GameRendererProjectionMixin 在每帧 renderLevel 时更新的 CPU 侧投影矩阵
 */
public final class ProjectionMatrixHolder {
    private static final Matrix4f PROJECTION = new Matrix4f();

    private ProjectionMatrixHolder() {
    }

    public static void set(Matrix4f matrix) {
        PROJECTION.set(matrix);
    }

    public static Matrix4f get() {
        return PROJECTION;
    }
}
