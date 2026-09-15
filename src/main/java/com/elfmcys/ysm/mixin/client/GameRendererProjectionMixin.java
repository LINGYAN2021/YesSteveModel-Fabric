package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.natives.render.ProjectionMatrixHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 26.1.2 的投影矩阵只在 GPU 侧。renderLevel 里通过
 * ProjectionMatrixBuffer.getBuffer(Matrix4f) 上传透视投影，
 * 包一层拿到 CPU 副本给原生渲染器（避免脆弱的 LVT 捕获）。
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererProjectionMixin {
    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
    private GpuBufferSlice ysm$captureProjection(ProjectionMatrixBuffer buffer, Matrix4f matrix,
                                                 Operation<GpuBufferSlice> original) {
        ProjectionMatrixHolder.set(matrix);
        return original.call(buffer, matrix);
    }
}
