package com.evandev.remi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.config.ReliableEmiConfigScreen;
import dev.emi.emi.config.EmiConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public class ReliableEmiClientNeoForge {
    public static void register(ModContainer container) {
        ReliableEmiConfig.load();

        NeoForge.EVENT_BUS.addListener(ReliableEmiClientNeoForge::onScreenOpening);

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (minecraft, parent) -> ReliableEmiConfigScreen.createScreen(parent));
    }

    private static void onScreenOpening(ScreenEvent.Opening event) {
        if (ReliableEmiConfig.emiOnlyInRecipeBook) {
            EmiConfig.enabled = false;
        }
    }
}