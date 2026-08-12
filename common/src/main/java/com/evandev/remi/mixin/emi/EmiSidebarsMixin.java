package com.evandev.remi.mixin.emi;

import com.evandev.remi.feature.workstation.WorkstationSidebarManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiSidebars;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EmiSidebars.class, remap = false)
public abstract class EmiSidebarsMixin {

    @Inject(method = "getStacks", at = @At("HEAD"), cancellable = true)
    private static void remi$getWorkstationStacks(SidebarType type, CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        if (WorkstationSidebarManager.WORKSTATION != null && type == WorkstationSidebarManager.WORKSTATION) {
            cir.setReturnValue(WorkstationSidebarManager.workstationStacks);
        }
    }
}
