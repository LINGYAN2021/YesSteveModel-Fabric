package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.animation.AnimationParallelTicker;
import com.elfmcys.ysm.util.RenderUtil;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void beforeRenderLevel(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker,
                                   boolean renderBlockOutline, CameraRenderState cameraState,
                                   Matrix4fc modelViewMatrix, GpuBufferSlice shaderFog, Vector4f fogColor,
                                   boolean renderSky, ChunkSectionsToRender sections, CallbackInfo ci) {
        if (YesSteveModel.isAvailable()) {
            RenderUtil.setRenderingLevel(true);
            AnimationParallelTicker.scheduleAll(deltaTracker.getGameTimeDeltaPartialTick(true));
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "RETURN"))
    private void afterRenderEntities(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker,
                                     boolean renderBlockOutline, CameraRenderState cameraState,
                                     Matrix4fc modelViewMatrix, GpuBufferSlice shaderFog, Vector4f fogColor,
                                     boolean renderSky, ChunkSectionsToRender sections, CallbackInfo ci) {
        if (YesSteveModel.isAvailable()) {
            AnimationParallelTicker.waitAll();
            RenderUtil.setRenderingLevel(false);
        }
    }
}
