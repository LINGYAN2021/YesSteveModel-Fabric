package com.elfmcys.ysm.mixin.client;

import com.elfmcys.ysm.client.input.YsmInputDispatcher;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void ysm$keyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        YsmInputDispatcher.fireKey(event.key(), event.scancode(), action, event.modifiers());
    }
}
