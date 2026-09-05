package com.evandev.remi.mixin.emi;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.screen.tooltip.TagTooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = TagEmiIngredient.class, remap = false)
public class TagEmiIngredientMixin {

    @Shadow
    private List<EmiStack> stacks;

    @ModifyVariable(method = "<init>(Lnet/minecraft/tags/TagKey;Ljava/util/List;J)V", at = @At("HEAD"), argsOnly = true)
    private static List<EmiStack> remi$filterEmptyStacksFromInit(List<EmiStack> stacks) {
        if (stacks != null && !stacks.isEmpty()) {
            boolean hasEmpty = false;
            for (EmiStack stack : stacks) {
                if (stack.isEmpty()) {
                    hasEmpty = true;
                    break;
                }
            }
            if (hasEmpty) {
                return stacks.stream().filter(stack -> !stack.isEmpty()).toList();
            }
        }
        return stacks;
    }

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

    @WrapOperation(
            method = "getTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private boolean remi$skipEmptyTagTooltipComponent(List<Object> list, Object element, Operation<Boolean> original) {
        if (element instanceof TagTooltipComponent && (this.stacks == null || this.stacks.isEmpty())) {
            return false;
        }
        return original.call(list, element);
    }
}