package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.renderer.pip.YsmPreviewRenderState;
import com.elfmcys.ysm.client.renderer.pip.YsmPreviewRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * NeoForge 版通过 RegisterPictureInPictureRenderersEvent 注册自定义 PiP 渲染器，
 * Fabric 没有对应事件，直接在 GuiRenderer 构造完成后向注册表 Map 中追加。
 */
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Shadow
    @Final
    private Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> pictureInPictureRenderers;

    @Shadow
    @Final
    private MultiBufferSource.BufferSource bufferSource;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ysm$registerPreviewRenderer(CallbackInfo ci) {
        // 每种用途一个渲染器实例：原版 PiP 渲染器只有一张离屏纹理，
        // 同帧同类多状态会互相覆盖，必须分道注册
        for (var type : YsmPreviewRenderState.laneTypes()) {
            this.pictureInPictureRenderers.put(type, new YsmPreviewRenderer(this.bufferSource));
        }
    }
}
