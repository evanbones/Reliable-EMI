package com.evandev.remi.mixin.minecraft;

import com.evandev.remi.config.ReliableEmiConfig;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.RecipeBookAction;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {

    @Shadow
    protected boolean visible;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(int width, int height, Minecraft minecraft, boolean widthTooNarrow, RecipeBookMenu<?, ?> menu, CallbackInfo ci) {
        if (ReliableEmiConfig.emiOnlyInRecipeBook) {
            this.visible = false;
            EmiConfig.recipeBookAction = RecipeBookAction.DEFAULT;
            boolean enabled = ReliableEmiConfig.emiOnlyInRecipeBookState;
            EmiConfig.enabled = enabled;
            if (enabled) {
                EmiScreenManager.recalculate();
            }
        }
    }

    @Inject(method = "toggleVisibility", at = @At("HEAD"), cancellable = true)
    private void onToggleVisibility(CallbackInfo ci) {
        if (ReliableEmiConfig.emiOnlyInRecipeBook) {
            this.visible = false;
            EmiConfig.recipeBookAction = RecipeBookAction.DEFAULT;
            ReliableEmiConfig.emiOnlyInRecipeBookState = !ReliableEmiConfig.emiOnlyInRecipeBookState;
            ReliableEmiConfig.save();
            boolean enabled = ReliableEmiConfig.emiOnlyInRecipeBookState;
            EmiConfig.enabled = enabled;
            if (enabled) {
                EmiScreenManager.recalculate();
            }
            ci.cancel();
        }
    }

    @Inject(method = "setVisible", at = @At("HEAD"), cancellable = true)
    private void onSetVisible(boolean visible, CallbackInfo ci) {
        if (ReliableEmiConfig.emiOnlyInRecipeBook) {
            this.visible = false;
            EmiConfig.recipeBookAction = RecipeBookAction.DEFAULT;
            ci.cancel();
        }
    }
}
