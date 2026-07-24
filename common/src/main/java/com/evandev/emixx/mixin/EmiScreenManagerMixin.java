package com.evandev.emixx.mixin;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.emixx.feature.creativemodetab.gui.CreativeModeTabGui;
import com.evandev.emixx.feature.stackgroup.EmiGroupStack;
import com.evandev.emixx.integration.emi.Layout;
import com.evandev.emixx.integration.emi.ScreenManager;
import com.evandev.emixx.integration.emi.StackManager;
import com.evandev.emixx.util.SidebarPanelWithScrollOffset;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EmiScreenManager.class, remap = false)
public abstract class EmiScreenManagerMixin {

    @Shadow
    private static List<? extends EmiIngredient> searchedStacks;

    @Shadow
    public static EmiScreenManager.SidebarPanel getSearchPanel() {
        throw new UnsupportedOperationException();
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "createScreenSpace", name = "headerOffset")
    private static int modifyHeaderOffset(int headerOffset, EmiScreenManager.SidebarPanel panel, Screen screen,
                                          List<Bounds> exclusion) {
        if (panel.getType() == SidebarType.INDEX && EmiPlusPlusConfig.enableCreativeModeTabs) {
            if (CreativeModeTabGui.currentTheme() == CreativeModeTabGui.TabTheme.DEFAULT) {
                return headerOffset + CreativeModeTabGui.CREATIVE_MODE_TAB_HEIGHT;
            }
        }
        return headerOffset;
    }

    @Redirect(method = "recalculate",
            at = @At(value = "FIELD", target = "Ldev/emi/emi/screen/EmiScreenManager;searchedStacks:Ljava/util/List;",
                    opcode = Opcodes.PUTSTATIC))
    private static void redirectStacksSourceToEmixx(List<? extends EmiIngredient> value) {
        if (getSearchPanel().getType() == SidebarType.INDEX) {
            searchedStacks = StackManager.displayedStacks;
        } else {
            searchedStacks = EmiSearch.stacks;
        }
    }

    @ModifyExpressionValue(method = "recalculate",
            at = @At(value = "FIELD", target = "Ldev/emi/emi/search/EmiSearch;stacks:Ljava/util/List;",
                    opcode = Opcodes.GETSTATIC))
    private static List<? extends EmiIngredient> redirectCachedStacksToEmixx(List<? extends EmiIngredient> original) {
        if (getSearchPanel().getType() == SidebarType.INDEX) {
            Layout.textureDirty = true;
            return StackManager.displayedStacks;
        }
        return original;
    }

    @Inject(method = "getSearchSource", at = @At(value = "RETURN"), cancellable = true)
    private static void redirectSearchSourceToEmixx(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        if (getSearchPanel().getType() == SidebarType.INDEX)
            cir.setReturnValue(StackManager.sourceStacks);
    }

    @Inject(at = @At("HEAD"), method = "addWidgets")
    private static void addEmiPlusPlusWidgets(Screen screen, CallbackInfo ci) {
        ScreenManager.onScreenInitialized(screen);
    }

    @Inject(at = @At("RETURN"), method = "mouseScrolled", cancellable = true)
    private static void mouseScrolled(double mouseX, double mouseY, double amount,
                                      CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() || ScreenManager.onMouseScrolled(mouseX, mouseY, amount));
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/stack/EmiIngredient;isEmpty()Z", ordinal = 0),
            method = "mouseReleased")
    private static boolean modifyMouseReleased(EmiIngredient instance, Operation<Boolean> original) {
        if (instance instanceof EmiGroupStack) {
            StackManager.onStackInteraction(instance);
        }
        return original.call(instance);
    }

    @ModifyVariable(at = @At("HEAD"), method = "createScreenSpace", argsOnly = true)
    private static Bounds modifyEmixxBounds(Bounds bounds, EmiScreenManager.SidebarPanel panel) {
        if (panel.getType() == SidebarType.INDEX && EmiPlusPlusConfig.enableCreativeModeTabs) {
            if (CreativeModeTabGui.currentTheme() == CreativeModeTabGui.TabTheme.VANILLA) {
                int tabSpace = 35;
                return new Bounds(
                        bounds.x() + tabSpace,
                        bounds.y(),
                        Math.max(0, bounds.width() - tabSpace),
                        bounds.height()
                );
            }
        }
        return bounds;
    }

    @ModifyVariable(method = "getHoveredStack(IIZZ)Ldev/emi/emi/api/stack/EmiStackInteraction;", at = @At(value = "STORE", ordinal = 1), name = "n")
    private static int addOffsetToHoveredStack(int n, @Local(name = "panel") EmiScreenManager.SidebarPanel panel) {
        if (EmiPlusPlusConfig.scrollInsteadOfPagination) {
            return n + ((SidebarPanelWithScrollOffset) panel).emixx$getScrollOffset();
        } else {
            return n;
        }
    }
}
