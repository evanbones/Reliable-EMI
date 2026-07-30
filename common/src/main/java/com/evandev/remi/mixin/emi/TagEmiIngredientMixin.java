package com.evandev.remi.mixin.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = TagEmiIngredient.class, remap = false)
public class TagEmiIngredientMixin {

    @Inject(method = "getEmiStacks", at = @At("RETURN"), cancellable = true)
    private void remi$filterEmptyStacksFromGetEmiStacks(CallbackInfoReturnable<List<EmiStack>> cir) {
        List<EmiStack> original = cir.getReturnValue();
        if (original != null && !original.isEmpty()) {
            boolean hasEmpty = false;
            for (EmiStack stack : original) {
                if (stack.isEmpty()) {
                    hasEmpty = true;
                    break;
                }
            }
            if (hasEmpty) {
                cir.setReturnValue(original.stream().filter(stack -> !stack.isEmpty()).toList());
            }
        }
    }
}
