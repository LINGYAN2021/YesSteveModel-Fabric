package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.renderer.RenderStateEntityLink;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 把实体引用直接挂在渲染状态对象上（渲染状态每帧新建，随 GC 回收，
 * 不会产生静态 Map 的泄漏问题）。
 */
@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateLinkMixin implements RenderStateEntityLink.Holder {
    @Unique
    @Nullable
    private Entity ysm$entity;

    @Unique
    private float ysm$partialTick = 1.0F;

    @Override
    public void ysm$link(Entity entity, float partialTick) {
        this.ysm$entity = entity;
        this.ysm$partialTick = partialTick;
    }

    @Override
    @Nullable
    public Entity ysm$linkedEntity() {
        return this.ysm$entity;
    }

    @Override
    public float ysm$linkedPartialTick() {
        return this.ysm$partialTick;
    }
}
