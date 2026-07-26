package com.evandev.emixx.mixin;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.EmiSearchWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EmiSearchWidget.class)
public class EmiSearchWidgetMixin {
    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;fill(IIIII)V", ordinal = 0))
    private void renderHighlightTop(EmiDrawContext instance, int x, int y, int width, int height, int color, Operation<Void> original) {
        x = x - EmiPlusPlusConfig.searchWidgetHorizontalPadding + 1;
        y = y - EmiPlusPlusConfig.searchWidgetVerticalPadding + 1;
        width = width + EmiPlusPlusConfig.searchWidgetHorizontalPadding * 2 - 2;
        original.call(instance, x, y, width, height, color);
    }
    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;fill(IIIII)V", ordinal = 1))
    private void renderHighlightBottom(EmiDrawContext instance, int x, int y, int width, int height, int color, Operation<Void> original) {
        x = x - EmiPlusPlusConfig.searchWidgetHorizontalPadding + 1;
        y = y + EmiPlusPlusConfig.searchWidgetVerticalPadding - 1;
        width = width + EmiPlusPlusConfig.searchWidgetHorizontalPadding * 2 - 2;
        original.call(instance, x, y, width, height, color);
    }
    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;fill(IIIII)V", ordinal = 2))
    private void renderHighlightLeft(EmiDrawContext instance, int x, int y, int width, int height, int color, Operation<Void> original) {
        x = x - EmiPlusPlusConfig.searchWidgetHorizontalPadding + 1;
        y = y - EmiPlusPlusConfig.searchWidgetVerticalPadding + 1;
        height = height + EmiPlusPlusConfig.searchWidgetVerticalPadding * 2 - 2;
        original.call(instance, x, y, width, height, color);
    }
    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;fill(IIIII)V", ordinal = 3))
    private void renderHighlightRight(EmiDrawContext instance, int x, int y, int width, int height, int color, Operation<Void> original) {
        x = x + EmiPlusPlusConfig.searchWidgetHorizontalPadding - 1;
        y = y - EmiPlusPlusConfig.searchWidgetVerticalPadding + 1;
        height = height + EmiPlusPlusConfig.searchWidgetVerticalPadding * 2 - 2;
        original.call(instance, x, y, width, height, color);
    }
}
