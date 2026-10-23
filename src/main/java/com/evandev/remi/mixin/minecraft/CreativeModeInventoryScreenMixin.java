package com.evandev.remi.mixin.minecraft;

import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {

    @Inject(method = "selectTab", at = @At("HEAD"))
    private void onSelectTab(CreativeModeTab tab, CallbackInfo ci) {
        CreativeModeTabManager.onCreativeModeInventoryScreenTabSelected(tab);
    }
}
