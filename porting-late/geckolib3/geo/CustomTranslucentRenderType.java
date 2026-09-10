package com.elfmcys.ysm.geckolib3.geo;

import net.minecraft.util.Util;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class CustomTranslucentRenderType extends RenderType {
    private static final Function<Identifier, CustomTranslucentRenderType> CUSTOM_TRANSLUCENT = Util.memoize(CustomTranslucentRenderType::new);

    private final boolean isOutline;
    private final Optional<RenderType> outline;

    private CustomTranslucentRenderType(Identifier textureLocation) {
        this(RenderType.entityTranslucent(textureLocation));
    }

    private CustomTranslucentRenderType(RenderType inner) {
        super("entity_translucent_ysm", inner.format(), inner.mode(), inner.bufferSize(), inner.affectsCrumbling(), false, inner::setupRenderState, inner::clearRenderState);
        this.isOutline = inner.isOutline();
        this.outline = inner.outline();
    }

    @Override
    public boolean isOutline() {
        return isOutline;
    }

    @Override
    @NotNull
    public Optional<RenderType> outline() {
        return outline;
    }

    public static CustomTranslucentRenderType create(Identifier textureLocation) {
        return CUSTOM_TRANSLUCENT.apply(textureLocation);
    }
}
