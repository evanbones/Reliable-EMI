package com.evandev.remi.mixin.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.tag.TagCategoryManager;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.recipe.EmiTagRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(value = EmiApi.class, remap = false)
public abstract class EmiApiMixin {

    @Invoker(value = "setPages", remap = false)
    public static void remi$setPages(Map<EmiRecipeCategory, List<EmiRecipe>> recipes, EmiIngredient stack) {
        throw new AssertionError();
    }

    @Inject(method = "displayRecipes", at = @At("HEAD"), cancellable = true)
    private static void remi$displayRecipes(EmiIngredient stack, CallbackInfo ci) {
        EmiIngredient targetStack = stack;
        if (targetStack instanceof EmiFavorite fav) {
            targetStack = fav.getStack();
        }
        if (targetStack instanceof TagEmiIngredient tag) {
            EmiRecipeCategory cat = ReliableEmiConfig.enableCategorizedTagPages
                    ? TagCategoryManager.getCategory(tag.key)
                    : VanillaPlugin.TAG;

            EmiRecipe foundRecipe = null;
            if (EmiApi.getRecipeManager() != null) {
                for (EmiRecipe recipe : EmiApi.getRecipeManager().getRecipes(cat)) {
                    if (recipe instanceof EmiTagRecipe tr && tr.key.equals(tag.key)) {
                        foundRecipe = recipe;
                        break;
                    }
                }
                if (foundRecipe == null && cat != VanillaPlugin.TAG) {
                    for (EmiRecipe recipe : EmiApi.getRecipeManager().getRecipes(VanillaPlugin.TAG)) {
                        if (recipe instanceof EmiTagRecipe tr && tr.key.equals(tag.key)) {
                            foundRecipe = recipe;
                            cat = VanillaPlugin.TAG;
                            break;
                        }
                    }
                }
            }

            if (foundRecipe != null) {
                remi$setPages(Map.of(cat, List.of(foundRecipe)), stack);
                ci.cancel();
            }
        }
    }
}
