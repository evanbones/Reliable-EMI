package com.evandev.remi.mixin.minecraft;

import com.evandev.remi.config.ReliableEmiConfig;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.RecipeBookAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("RETURN"))
    private void onInit(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        if (ReliableEmiConfig.emiOnlyInRecipeBook) {
            EmiConfig.recipeBookAction = RecipeBookAction.DEFAULT;
            if (this instanceof RecipeUpdateListener) {
                // For screens with recipe books, RecipeBookComponentMixin.onInit handles EMI state
                return;
            }
            // For screens without recipe books (e.g. Chests, Anvils, Main Menu), hide EMI
            EmiConfig.enabled = false;
        }
    }
}
