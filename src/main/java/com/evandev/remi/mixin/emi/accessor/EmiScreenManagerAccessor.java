package com.evandev.remi.mixin.emi.accessor;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(value = EmiScreenManager.class, remap = false)
public interface EmiScreenManagerAccessor {

    @Accessor("panels")
    static List<EmiScreenManager.SidebarPanel> getPanels() {
        throw new UnsupportedOperationException();
    }

    @Invoker("give")
    static boolean remi$give(EmiStack stack, int amount, int mode) {
        throw new UnsupportedOperationException();
    }
}
