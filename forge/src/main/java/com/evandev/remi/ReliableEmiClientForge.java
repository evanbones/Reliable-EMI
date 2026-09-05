package com.evandev.remi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.config.ReliableEmiConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public class ReliableEmiClientForge {
    public static void register() {
        ReliableEmiConfig.load();

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> ReliableEmiConfigScreen.createScreen(parent)));
    }
}
