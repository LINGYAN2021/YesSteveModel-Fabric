package com.elfmcys.ysm.api.rendering.v0.event;

import com.elfmcys.ysm.api.rendering.v0.TargetKind;
import com.elfmcys.ysm.geckolib3.geo.GeoRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.elfmcys.ysm.event.bus.YsmCancelable;
import com.elfmcys.ysm.event.bus.YsmEvent;
import org.jetbrains.annotations.ApiStatus;

@YsmCancelable
public class RenderLayerEvent extends YsmEvent {
    private final Object target;
    private final TargetKind targetKind;
    private final GeoRenderData renderData;
    private final PoseStack poseStack;
    private final MultiBufferSource buffer;
    private final int packedLight;
    private final int overlay;

    public RenderLayerEvent(Object target, TargetKind targetKind, GeoRenderData renderData, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int overlay) {
        this.target = target;
        this.targetKind = targetKind;
        this.renderData = renderData;
        this.poseStack = poseStack;
        this.buffer = buffer;
        this.packedLight = packedLight;
        this.overlay = overlay;
    }

    public Object target() {
        return target;
    }

    public TargetKind targetKind() {
        return targetKind;
    }

    public GeoRenderData renderData() {
        return renderData;
    }

    public PoseStack poseStack() {
        return poseStack;
    }

    public MultiBufferSource buffer() {
        return buffer;
    }

    public int packedLight() {
        return packedLight;
    }

    public int overlay() {
        return overlay;
    }
}
