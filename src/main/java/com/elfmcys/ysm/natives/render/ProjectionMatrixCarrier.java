package com.elfmcys.ysm.natives.render;

import org.joml.Matrix4f;

/**
 * 挂在 GpuBufferSlice 上的投影矩阵 CPU 副本（参考官方实现）。
 * ProjectionMatrixBuffer.writeBuffer 写入 GPU 时同步附带，
 * 之后可随时从 RenderSystem.getProjectionMatrixBuffer() 取回当前投影矩阵。
 */
public interface ProjectionMatrixCarrier {
    Matrix4f ysm$getProjectionMatrix();

    void ysm$setProjectionMatrix(Matrix4f matrix);
}
