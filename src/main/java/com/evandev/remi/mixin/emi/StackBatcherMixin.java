package com.evandev.remi.mixin.emi;

import com.evandev.remi.feature.stackgroup.EmiGroupStack;
import dev.emi.emi.screen.StackBatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StackBatcher.class, remap = false)
public class StackBatcherMixin {

    @Inject(method = "render*", at = @At("HEAD"))
    private void beginRender(CallbackInfo ci) {
        EmiGroupStack.deferIndicators = true;
    }

    @Inject(method = "render*", at = @At("RETURN"))
    private void endRender(CallbackInfo ci) {
        EmiGroupStack.deferIndicators = false;
    }

    @Inject(method = "draw", at = @At("RETURN"))
    private void drawIndicators(CallbackInfo ci) {
        EmiGroupStack.drawDeferredIndicators();
    }
}
