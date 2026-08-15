package com.evandev.remi.mixin.emi;

import com.evandev.remi.integration.emi.StackManager;
import dev.emi.emi.runtime.EmiFavorites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EmiFavorites.class, remap = false)
public class EmiFavoritesMixin {

    @Inject(method = "load", at = @At("TAIL"))
    private static void remi$invalidateOnLoad(CallbackInfo ci) {
        StackManager.invalidateStacks();
    }

    @Inject(method = "removeFavorite", at = @At("TAIL"))
    private static void remi$invalidateOnRemove(CallbackInfoReturnable<Boolean> cir) {
        StackManager.invalidateStacks();
    }

    @Inject(method = "addFavoriteAt", at = @At("TAIL"))
    private static void remi$invalidateOnAddAt(CallbackInfo ci) {
        StackManager.invalidateStacks();
    }

    @Inject(method = "addFavorite(Ldev/emi/emi/api/stack/EmiIngredient;Ldev/emi/emi/api/recipe/EmiRecipe;)V",
            at = @At("TAIL"))
    private static void remi$invalidateOnAdd(CallbackInfo ci) {
        StackManager.invalidateStacks();
    }

    @Inject(method = "updateSynthetic", at = @At("TAIL"))
    private static void remi$invalidateOnSynthetic(CallbackInfo ci) {
        StackManager.invalidateStacks();
    }
}
