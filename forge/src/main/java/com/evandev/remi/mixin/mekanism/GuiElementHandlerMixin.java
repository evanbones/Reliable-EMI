package com.evandev.remi.mixin.mekanism;

import com.evandev.remi.feature.creativemodetab.gui.itemtab.ItemTabNavigationBar;
import com.evandev.remi.gui.components.ImageButton;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.client.jei.GuiElementHandler;
import net.minecraft.client.gui.components.AbstractWidget;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GuiElementHandler.class, remap = false)
public class GuiElementHandlerMixin {

    @WrapOperation(
            method = "getAreasFor",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/gui/components/AbstractWidget;visible:Z",
                    remap = true, opcode = Opcodes.GETFIELD)
    )
    private static boolean skipEmiPlusPlusButtons(AbstractWidget instance, Operation<Boolean> original) {
        if (instance.getClass().getName().startsWith("com.evandev")) {
            return false;
        }

        if (instance instanceof ImageButton || instance instanceof ItemTabNavigationBar) {
            return false;
        }

        return original.call(instance);
    }
}