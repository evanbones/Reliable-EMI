package concerrox.emixx.neoforge.mixin.mekanism;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import concerrox.emixx.content.creativemodetab.gui.itemtab.ItemTabNavigationBar;
import concerrox.emixx.gui.components.ImageButton;
import mekanism.client.recipe_viewer.GuiElementHandler;
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
        if (instance.getClass().getName().startsWith("concerrox.emixx")) {
            return false;
        }

        if (instance instanceof ImageButton || instance instanceof ItemTabNavigationBar) {
            return false;
        }

        return original.call(instance);
    }
}