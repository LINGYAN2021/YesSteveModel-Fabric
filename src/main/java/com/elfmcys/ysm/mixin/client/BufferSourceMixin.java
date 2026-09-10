package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.accessor.IExtendedBufferSource;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.SequencedMap;

@Mixin(MultiBufferSource.BufferSource.class)
public class BufferSourceMixin implements IExtendedBufferSource {
    @Shadow
    @Final
    protected SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers;

    @Unique
    public void endBatchFixedRenderType() {
        for (RenderType type : fixedBuffers.keySet()) {
            ((MultiBufferSource.BufferSource) (Object) this).endBatch(type);
        }
    }
}
