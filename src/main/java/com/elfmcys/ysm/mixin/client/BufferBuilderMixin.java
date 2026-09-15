package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.accessor.VertexBufferAccessor;
import com.elfmcys.ysm.buffer.NativeBuffer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * 26.1.2 的 BufferBuilder 改为裸指针写入：ByteBufferBuilder.reserve 返回
 * 内存地址并推进 writeOffset，BufferBuilder.vertexPointer 指向当前顶点。
 * 原生渲染路径直接向 reserve 出的区域 memcpy，再用 advance 提交顶点数。
 */
@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements VertexBufferAccessor {
    @Shadow
    @Final
    private ByteBufferBuilder buffer;

    @Shadow
    private long vertexPointer;

    @Shadow
    private int vertices;

    @Shadow
    @Final
    private VertexFormat format;

    @Shadow
    @Final
    private int vertexSize;

    @Shadow
    private boolean building;

    @Unique
    @Override
    public boolean ysm$ok() {
        return this.building;
    }

    @Unique
    @Override
    public VertexFormat ysm$vertexFormat() {
        return this.format;
    }

    @Unique
    @Override
    public NativeBuffer ysm$reserve(int vertexCount) {
        int bytes = vertexCount * this.vertexSize;
        long pointer = this.buffer.reserve(bytes);
        this.vertexPointer = pointer;
        return NativeBuffer.borrow(MemoryUtil.memByteBuffer(pointer, bytes));
    }

    @Unique
    @Override
    public void ysm$advance(int vertexCount) {
        this.vertices += vertexCount;
        this.vertexPointer += (long) vertexCount * this.vertexSize;
    }
}
