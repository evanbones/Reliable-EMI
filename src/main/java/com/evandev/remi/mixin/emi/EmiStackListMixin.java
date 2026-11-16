package com.evandev.remi.mixin.emi;

import com.evandev.remi.feature.stackgroup.EmiGroupStack;
import com.evandev.remi.integration.emi.StackManager;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiReloadManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiStackList.class, remap = false)
public abstract class EmiStackListMixin {

    @Inject(method = "bakeFiltered", at = @At("TAIL"))
    private static void remi$invalidateGroupVisibilityCaches(CallbackInfo ci) {
        EmiGroupStack.onStackFilterChanged();
        if (EmiReloadManager.isLoaded()) {
            StackManager.onFilteredStacksChanged();
        }
    }
}
