package com.evandev.emixx.mixin;

import com.evandev.emixx.feature.stackgroup.EmiGroupStack;
import dev.emi.emi.registry.EmiStackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiStackList.class, remap = false)
public abstract class EmiStackListMixin {

    @Inject(method = "bakeFiltered", at = @At("TAIL"))
    private static void emixx$invalidateGroupVisibilityCaches(CallbackInfo ci) {
        EmiGroupStack.onStackFilterChanged();
    }
}
