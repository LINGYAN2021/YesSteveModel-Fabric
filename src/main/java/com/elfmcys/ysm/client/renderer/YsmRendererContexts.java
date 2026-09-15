package com.elfmcys.ysm.client.renderer;

import com.elfmcys.ysm.mixin.client.EntityRenderDispatcherAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * 26.1.2 中 EntityRendererProvider.Context 的全部组成部分都由
 * EntityRenderDispatcher 持有，这里通过访问器 mixin 取回并组装，
 * 避免维护第二份 EquipmentAssetManager 之类的资源。
 */
public final class YsmRendererContexts {
    private static EntityRendererProvider.Context context;

    private YsmRendererContexts() {
    }

    public static EntityRendererProvider.Context get() {
        if (context == null) {
            Minecraft mc = Minecraft.getInstance();
            var dispatcher = mc.getEntityRenderDispatcher();
            var accessor = (EntityRenderDispatcherAccessor) dispatcher;
            context = new EntityRendererProvider.Context(
                    dispatcher,
                    accessor.ysm$blockModelResolver(),
                    accessor.ysm$itemModelResolver(),
                    accessor.ysm$mapRenderer(),
                    mc.getResourceManager(),
                    mc.getEntityModels(),
                    accessor.ysm$equipmentAssets(),
                    accessor.ysm$atlasManager(),
                    mc.font,
                    accessor.ysm$playerSkinRenderCache());
        }
        return context;
    }
}
