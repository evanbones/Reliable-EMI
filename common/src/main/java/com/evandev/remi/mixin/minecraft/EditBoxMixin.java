package com.evandev.remi.mixin.minecraft;

import com.evandev.ReliableEmi;
import com.evandev.remi.config.ReliableEmiConfig;
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
    private static final WidgetSprites remi$SPRITES = new WidgetSprites(ReliableEmi.res("widget/text_field"), ReliableEmi.res("widget/text_field_highlighted"));

   @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
   private void drawSearchWidgetBackground(GuiGraphics instance, ResourceLocation sprite, int x, int y, int width, int height, Operation<Void> original) {
        EditBox editBox = (EditBox)(Object)this;
        if (editBox instanceof EmiSearchWidget) {
            int horizontalPadding = ReliableEmiConfig.searchWidgetHorizontalPadding;
            int verticalPadding = ReliableEmiConfig.searchWidgetVerticalPadding;
            sprite = remi$SPRITES.get(editBox.isActive(), editBox.isFocused());
            x = x - horizontalPadding;
            y = y - verticalPadding;
            width = width + horizontalPadding * 2;
            height = height + verticalPadding * 2;
        }
        original.call(instance, sprite, x, y, width, height);
   }

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I", ordinal = 0), require = 0)
    private int drawSuggestionString(GuiGraphics instance, Font font, String text, int x, int y, int color, Operation<Integer> original) {
        EditBox editBox = (EditBox)(Object)this;
        if (editBox instanceof EmiSearchWidget) {
            color = ReliableEmiConfig.searchWidgetSuggestionTextColor;
        }
        return original.call(instance, font, text, x, y, color);
    }

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", ordinal = 0), require = 0)
    private int drawSuggestionStringNeo(GuiGraphics instance, Font font, String text, int x, int y, int color, boolean dropShadow, Operation<Integer> original) {
        EditBox editBox = (EditBox)(Object)this;
        if (editBox instanceof EmiSearchWidget) {
            color = ReliableEmiConfig.searchWidgetSuggestionTextColor;
        }
        return original.call(instance, font, text, x, y, color, dropShadow);
    }

    @ModifyVariable(method = "renderWidget", at = @At(value = "STORE"), name = "l1")
    private int textColor(int color) {
        EditBox editBox = (EditBox)(Object)this;
        if (editBox instanceof EmiSearchWidget) {
            color = ReliableEmiConfig.searchWidgetTextColor;
        }
        return color;
    }
}
