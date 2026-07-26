package com.evandev.emixx.mixin.minecraft;

import com.evandev.EmiPlusPlus;
import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.screen.widget.EmiSearchWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EditBox.class)
public class EditBoxMixin {
    @Unique
    private static final WidgetSprites emixx$SPRITES = new WidgetSprites(EmiPlusPlus.res("widget/text_field"), EmiPlusPlus.res("widget/text_field_highlighted"));

   @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
   private void drawSearchWidgetBackground(GuiGraphics instance, ResourceLocation sprite, int x, int y, int width, int height, Operation<Void> original) {
        EditBox editBox = (EditBox)(Object)this;
        if (editBox instanceof EmiSearchWidget) {
            int horizontalPadding = EmiPlusPlusConfig.searchWidgetHorizontalPadding;
            int verticalPadding = EmiPlusPlusConfig.searchWidgetVerticalPadding;
            sprite = emixx$SPRITES.get(editBox.isActive(), editBox.isFocused());
            x = x - horizontalPadding;
            y = y - verticalPadding;
            width = width + horizontalPadding * 2;
            height = height + verticalPadding * 2;
        }
        original.call(instance, sprite, x, y, width, height);
   }

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I", ordinal = 0))
    private int drawSuggestionString(GuiGraphics instance, Font font, String text, int x, int y, int color, Operation<Integer> original) {
        EditBox editBox = (EditBox)(Object)this;
        if (editBox instanceof EmiSearchWidget) {
            color = EmiPlusPlusConfig.searchWidgetSuggestionTextColor;
        }
        return original.call(instance, font, text, x, y, color);
    }

    @ModifyVariable(method = "renderWidget", at = @At(value = "STORE"), ordinal = 2)
    private int textColor(int color) {
        EditBox editBox = (EditBox)(Object)this;
        if (editBox instanceof EmiSearchWidget) {
            color = EmiPlusPlusConfig.searchWidgetTextColor;
        }
        return color;
    }
}
