package com.elfmcys.ysm.mixin.client;

import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Arrow.class)
public interface ArrowEntityAccessor {
    @Invoker("getPotionContents")
    PotionContents ysm$getPotionContents();
}
