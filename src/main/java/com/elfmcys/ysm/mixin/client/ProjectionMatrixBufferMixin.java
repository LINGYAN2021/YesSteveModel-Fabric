package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.natives.render.ProjectionMatrixCarrier;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 投影矩阵上传 GPU 时把 CPU 副本附带在返回的 slice 上（参考官方实现）。
 * 这样关卡透视、PiP 正交等所有投影切换都能被原生渲染器拿到正确矩阵。
 */
@Mixin(ProjectionMatrixBuffer.class)
public abstract class ProjectionMatrixBufferMixin {
    @Inject(method = "writeBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;",
            at = @At("RETURN"))
    private void ysm$attachMatrix(Matrix4f matrix, CallbackInfoReturnable<GpuBufferSlice> cir) {
        GpuBufferSlice slice = cir.getReturnValue();
        if (((Object) slice) instanceof ProjectionMatrixCarrier carrier) {
            carrier.ysm$setProjectionMatrix(matrix);
        }
    }
}
