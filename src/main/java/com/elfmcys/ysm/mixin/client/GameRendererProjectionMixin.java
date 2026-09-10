package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.natives.render.ProjectionMatrixHolder;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * 26.1.2 的投影矩阵只在 GPU 侧，这里在 renderLevel 设置投影时捕获一份 CPU 副本给原生渲染器
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererProjectionMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void ysm$captureProjection(DeltaTracker deltaTracker, CallbackInfo ci, Matrix4f projectionMatrix) {
        ProjectionMatrixHolder.set(projectionMatrix);
    }
}
