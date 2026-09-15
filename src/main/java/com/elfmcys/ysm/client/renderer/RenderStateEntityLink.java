package com.elfmcys.ysm.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * 26.1.2 的 EntityRenderState 不再持有实体引用，YSM 在
 * EntityRenderDispatcher.extractEntity 时把实体与渲染状态关联起来，
 * 供 submit 阶段的替换渲染逻辑反查。
 * <p>
 * 链接通过 {@code EntityRenderStateLinkMixin} 直接存储在状态对象上
 * （渲染状态每帧新建，随 GC 回收，不会泄漏）；本类只是对外的静态访问入口。
 */
public final class RenderStateEntityLink {
    private RenderStateEntityLink() {
    }

    /**
     * 由 EntityRenderStateLinkMixin 实现，挂在每个渲染状态对象上。
     */
    public interface Holder {
        void ysm$link(Entity entity, float partialTick);

        @Nullable
        Entity ysm$linkedEntity();

        float ysm$linkedPartialTick();
    }

    public static void record(EntityRenderState state, Entity entity, float partialTick) {
        if (state instanceof Holder holder) {
            holder.ysm$link(entity, partialTick);
        }
    }

    @Nullable
    public static Entity entityOf(EntityRenderState state) {
        return state instanceof Holder holder ? holder.ysm$linkedEntity() : null;
    }

    public static float partialTickOf(EntityRenderState state) {
        return state instanceof Holder holder ? holder.ysm$linkedPartialTick() : 1.0F;
    }
}
