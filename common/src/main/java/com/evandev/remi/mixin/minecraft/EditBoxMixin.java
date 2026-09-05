package com.evandev.remi.mixin.minecraft;

import com.evandev.ReliableEmi;
import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.util.GuiGraphicsUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.screen.widget.EmiSearchWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
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
    private static final ResourceLocation remi$TEXT_FIELD = ReliableEmi.res("widget/text_field");
    @Unique
    private static final ResourceLocation remi$TEXT_FIELD_HIGHLIGHTED = ReliableEmi.res("widget/text_field_highlighted");

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0))
    private void drawSearchWidgetBackground(GuiGraphics instance, int x1, int y1, int x2, int y2, int color,
                                            Operation<Void> original) {
        EditBox editBox = (EditBox) (Object) this;
        if (editBox instanceof EmiSearchWidget && !ReliableEmiConfig.searchWidgetUseVanillaTexture) {
            int horizontalPadding = ReliableEmiConfig.searchWidgetHorizontalPadding;
            int verticalPadding = ReliableEmiConfig.searchWidgetVerticalPadding;
            ResourceLocation sprite = editBox.isActive() && editBox.isFocused()
                    ? remi$TEXT_FIELD_HIGHLIGHTED : remi$TEXT_FIELD;
            GuiGraphicsUtils.blitSprite(instance, sprite,
                    editBox.getX() - 1 - horizontalPadding,
                    editBox.getY() - 1 - verticalPadding,
                    editBox.getWidth() + 2 + horizontalPadding * 2,
                    editBox.getHeight() + 2 + verticalPadding * 2);
            return;
        }
        original.call(instance, x1, y1, x2, y2, color);
    }

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 1))
    private void skipSearchWidgetInnerFill(GuiGraphics instance, int x1, int y1, int x2, int y2, int color,
                                           Operation<Void> original) {
        EditBox editBox = (EditBox) (Object) this;
        if (editBox instanceof EmiSearchWidget && !ReliableEmiConfig.searchWidgetUseVanillaTexture) {
            return;
        }
        original.call(instance, x1, y1, x2, y2, color);
    }

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I", ordinal = 0), require = 0)
    private int drawSuggestionString(GuiGraphics instance, Font font, String text, int x, int y, int color, Operation<Integer> original) {
        EditBox editBox = (EditBox) (Object) this;
        if (editBox instanceof EmiSearchWidget) {
            color = ReliableEmiConfig.searchWidgetSuggestionTextColor;
        }
        return original.call(instance, font, text, x, y, color);
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