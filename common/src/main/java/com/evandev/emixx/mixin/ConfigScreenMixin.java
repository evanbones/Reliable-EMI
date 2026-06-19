package com.evandev.emixx.mixin;

import com.evandev.emixx.config.ConfigScreenManager;
import dev.emi.emi.screen.ConfigScreen;
import dev.emi.emi.screen.widget.config.ConfigSearch;
import dev.emi.emi.screen.widget.config.ListWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ConfigScreen.class, remap = false)
public class ConfigScreenMixin {

    @Shadow
    private ConfigSearch search;

    @ModifyArg(method = "init", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/screen/ConfigScreen;addWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"),
            remap = true)
    public GuiEventListener init(GuiEventListener widget) {
        if (widget instanceof ListWidget list)
            ConfigScreenManager.injectConfigScreen((ConfigScreen) (Object) this, list, this.search);
        return widget;
    }
}
