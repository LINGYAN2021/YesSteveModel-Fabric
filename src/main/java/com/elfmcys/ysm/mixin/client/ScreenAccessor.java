package com.elfmcys.ysm.mixin.client;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * 26.1.2 的 Screen.renderables 是私有字段且无 getter，
 * 部分 YSM 界面需要在 super 渲染之外自行遍历Renderable（如自定义 tooltip）。
 */
@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("renderables")
    List<Renderable> ysm$renderables();
}
