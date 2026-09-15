package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.input.YsmInputDispatcher;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void ysm$onButton(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        YsmInputDispatcher.fireMouse(buttonInfo.button(), action, buttonInfo.modifiers());
    }
}
