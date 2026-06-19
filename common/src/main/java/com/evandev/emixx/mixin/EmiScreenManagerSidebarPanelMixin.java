package com.evandev.emixx.mixin;

import com.evandev.emixx.integration.emi.ScreenManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EmiScreenManager.SidebarPanel.class, remap = false)
public abstract class EmiScreenManagerSidebarPanelMixin {

    @Shadow
    public abstract SidebarType getType();

    @Shadow
    public abstract List<EmiScreenManager.ScreenSpace> getSpaces();

    @WrapOperation(method = "drawHeader", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/EmiRenderHelper;getPageText(III)Lnet/minecraft/network/chat/Component;", remap = true))
    private Component replaceIndexHeader(int page, int total, int maxWidth, Operation<Component> original) {
        if (getType() == SidebarType.INDEX && ScreenManager.customIndexTitle != null)
            return ScreenManager.customIndexTitle;
        return original.call(page, total, maxWidth);
    }

    @Inject(at = @At("TAIL"), method = "setSpaces")
    private void addEmiPlusPlusWidgets(EmiScreenManager.ScreenSpace main, List<EmiScreenManager.ScreenSpace> subpanels, CallbackInfo ci) {
        getSpaces().stream()
                .filter(space -> space.getType() == SidebarType.INDEX)
                .findFirst()
                .ifPresent(ScreenManager::onIndexScreenSpaceCreated);
    }
}
