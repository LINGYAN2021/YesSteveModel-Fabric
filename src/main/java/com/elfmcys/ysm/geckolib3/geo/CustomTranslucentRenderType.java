package com.elfmcys.ysm.geckolib3.geo;

import net.minecraft.util.Util;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

/**
 * 26.1.2 的 RenderType 不再可子类化（create 为包私有），
 * 改为对 entityTranslucent 的缓存工厂，保持与旧 API 相同的调用点。
 */
public final class CustomTranslucentRenderType {
    private static final Function<Identifier, RenderType> CUSTOM_TRANSLUCENT = Util.memoize((Function<Identifier, RenderType>) RenderTypes::entityTranslucent);

    private CustomTranslucentRenderType() {
    }

    public static RenderType create(Identifier textureLocation) {
        return CUSTOM_TRANSLUCENT.apply(textureLocation);
    }
}
