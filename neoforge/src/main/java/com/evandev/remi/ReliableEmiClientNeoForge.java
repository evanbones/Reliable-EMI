package com.evandev.remi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.config.ReliableEmiConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ReliableEmiClientNeoForge {
    public static void register(ModContainer container) {
        ReliableEmiConfig.load();

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (minecraft, parent) -> ReliableEmiConfigScreen.createScreen(parent));
    }
}