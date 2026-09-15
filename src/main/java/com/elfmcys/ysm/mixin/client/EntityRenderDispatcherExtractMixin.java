package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.renderer.RenderStateEntityLink;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherExtractMixin {
    /**
     * 26.1.2 的 EntityRenderState 不持有实体引用，这里在提取阶段记录下来，
     * 供 submit 阶段的替换渲染逻辑反查实体与 partialTick。
     */
    @Inject(method = "extractEntity", at = @At("RETURN"))
    private void ysm$recordEntity(Entity entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
        RenderStateEntityLink.record(cir.getReturnValue(), entity, partialTick);
    }
}
