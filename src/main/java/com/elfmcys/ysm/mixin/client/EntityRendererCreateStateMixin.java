package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.renderer.RenderStateEntityLink;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.1.2 的界面纸娃娃（背包、模型选择界面左侧预览）走的是
 * {@code renderer.createRenderState(entity, partialTick)}，绕过了
 * EntityRenderDispatcher.extractEntity，因此替换渲染的 mixin 查不到实体，
 * 纸娃娃会退回原版渲染（等于什么都不显示）。这里在创建阶段补上链接。
 */
@Mixin(EntityRenderer.class)
public class EntityRendererCreateStateMixin {
    @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN"))
    private void ysm$linkCreatedState(Entity entity, float partialTick,
                                      CallbackInfoReturnable<EntityRenderState> cir) {
        RenderStateEntityLink.record(cir.getReturnValue(), entity, partialTick);
    }
}
