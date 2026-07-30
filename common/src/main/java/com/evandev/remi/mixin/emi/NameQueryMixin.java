package com.evandev.remi.mixin.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.search.NameQuery;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NameQuery.class, remap = false)
public class NameQueryMixin {

    @Shadow
    @Final
    private String name;

    @Inject(method = "matchesUnbaked", at = @At("RETURN"), cancellable = true)
    private void matchesUnbakedIdFix(EmiStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() || stack.getId().getPath().contains(this.name));
    }
}
