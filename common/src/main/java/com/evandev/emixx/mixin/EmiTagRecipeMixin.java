package com.evandev.emixx.mixin;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.emixx.feature.tag.TagCategoryManager;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.recipe.EmiTagRecipe;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EmiTagRecipe.class, remap = false)
public class EmiTagRecipeMixin {

    @Shadow
    @Final
    public TagKey<?> key;

    @Inject(method = "getCategory", at = @At("HEAD"), cancellable = true)
    private void redirectGetCategory(CallbackInfoReturnable<EmiRecipeCategory> cir) {
        if (!EmiPlusPlusConfig.enableCategorizedTagPages) {
            return;
        }
        cir.setReturnValue(TagCategoryManager.getCategory(this.key));
    }
}
