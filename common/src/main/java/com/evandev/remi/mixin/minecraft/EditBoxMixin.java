package com.evandev.remi.mixin.minecraft;

import com.evandev.ReliableEmi;
import com.evandev.remi.config.ReliableEmiConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.screen.widget.EmiSearchWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiFunction;

@Mixin(EditBox.class)
public class EditBoxMixin {
    @Unique
    private static final WidgetSprites remi$SPRITES = new WidgetSprites(
            ReliableEmi.res("widget/text_field"),
            ReliableEmi.res("widget/text_field_highlighted")
    );

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private void drawSearchWidgetBackground(GuiGraphics instance, ResourceLocation sprite, int x, int y, int width, int height, Operation<Void> original) {
        EditBox editBox = (EditBox) (Object) this;
        if (editBox instanceof EmiSearchWidget && !ReliableEmiConfig.searchWidgetUseVanillaTexture) {
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
        EditBox editBox = (EditBox) (Object) this;
        if (editBox instanceof EmiSearchWidget) {
            color = ReliableEmiConfig.searchWidgetSuggestionTextColor;
        }
        return original.call(instance, font, text, x, y, color);
    }

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", ordinal = 0), require = 0)
    private int drawSuggestionStringNeo(GuiGraphics instance, Font font, String text, int x, int y, int color, boolean dropShadow, Operation<Integer> original) {
        EditBox editBox = (EditBox) (Object) this;
        if (editBox instanceof EmiSearchWidget) {
            color = ReliableEmiConfig.searchWidgetSuggestionTextColor;
        }
        return original.call(instance, font, text, x, y, color, dropShadow);
    }

    @ModifyExpressionValue(
            method = "renderWidget",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;textColor:I", opcode = Opcodes.GETFIELD)
    )
    private int overrideTextColor(int original) {
        EditBox editBox = (EditBox) (Object) this;
        return (editBox instanceof EmiSearchWidget) ? ReliableEmiConfig.searchWidgetTextColor : original;
    }

    @WrapOperation(
            method = "renderWidget",
            at = @At(value = "INVOKE", target = "Ljava/util/function/BiFunction;apply(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object overrideFormattedTextColor(
            BiFunction<String, Integer, FormattedCharSequence> instance,
            Object text,
            Object displayPos,
            Operation<FormattedCharSequence> original
    ) {
        FormattedCharSequence sequence = original.call(instance, text, displayPos);
        EditBox editBox = (EditBox) (Object) this;
        if (editBox instanceof EmiSearchWidget) {
            int customColor = ReliableEmiConfig.searchWidgetTextColor;
            return (FormattedCharSequence) sink -> sequence.accept((index, style, codePoint) -> {
                if (style.getColor() != null && style.getColor().getValue() == 0xFFFFFF) {
                    style = style.withColor(customColor);
                }
                return sink.accept(index, style, codePoint);
            });
        }
        return sequence;
    }
}