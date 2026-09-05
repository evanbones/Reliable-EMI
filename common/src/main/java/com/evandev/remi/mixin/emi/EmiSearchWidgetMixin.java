package com.evandev.remi.mixin.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.EmiSearchWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EmiSearchWidget.class)
public class EmiSearchWidgetMixin {
    @WrapOperation(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;fill(IIIII)V", ordinal = 0, remap = false))
    private void renderHighlightTop(EmiDrawContext instance, int x, int y, int width, int height, int color, Operation<Void> original) {
        if (!ReliableEmiConfig.searchWidgetUseVanillaTexture) {
            x = x - ReliableEmiConfig.searchWidgetHorizontalPadding + 1;
            y = y - ReliableEmiConfig.searchWidgetVerticalPadding + 1;
            width = width + ReliableEmiConfig.searchWidgetHorizontalPadding * 2 - 2;
        }
        original.call(instance, x, y, width, height, color);
    }

    @WrapOperation(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;fill(IIIII)V", ordinal = 1, remap = false))
    private void renderHighlightBottom(EmiDrawContext instance, int x, int y, int width, int height, int color, Operation<Void> original) {
        if (!ReliableEmiConfig.searchWidgetUseVanillaTexture) {
            x = x - ReliableEmiConfig.searchWidgetHorizontalPadding + 1;
            y = y + ReliableEmiConfig.searchWidgetVerticalPadding - 1;
            width = width + ReliableEmiConfig.searchWidgetHorizontalPadding * 2 - 2;
        }
        original.call(instance, x, y, width, height, color);
    }

    @WrapOperation(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;fill(IIIII)V", ordinal = 2, remap = false))
    private void renderHighlightLeft(EmiDrawContext instance, int x, int y, int width, int height, int color, Operation<Void> original) {
        if (!ReliableEmiConfig.searchWidgetUseVanillaTexture) {
            x = x - ReliableEmiConfig.searchWidgetHorizontalPadding + 1;
            y = y - ReliableEmiConfig.searchWidgetVerticalPadding + 1;
            height = height + ReliableEmiConfig.searchWidgetVerticalPadding * 2 - 2;
        }
        original.call(instance, x, y, width, height, color);
    }

    @WrapOperation(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;fill(IIIII)V", ordinal = 3, remap = false))
    private void renderHighlightRight(EmiDrawContext instance, int x, int y, int width, int height, int color, Operation<Void> original) {
        if (!ReliableEmiConfig.searchWidgetUseVanillaTexture) {
            x = x + ReliableEmiConfig.searchWidgetHorizontalPadding - 1;
            y = y - ReliableEmiConfig.searchWidgetVerticalPadding + 1;
            height = height + ReliableEmiConfig.searchWidgetVerticalPadding * 2 - 2;
        }
        original.call(instance, x, y, width, height, color);
    }
}
