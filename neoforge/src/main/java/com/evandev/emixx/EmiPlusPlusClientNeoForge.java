package com.evandev.emixx;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.emixx.feature.stackgroup.gui.StackGroupConfigScreen;
import dev.emi.emi.config.EmiConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public class EmiPlusPlusClientNeoForge {
    public static void register(ModContainer container) {
        EmiPlusPlusConfig.load();

        NeoForge.EVENT_BUS.addListener(EmiPlusPlusClientNeoForge::onScreenOpening);

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (minecraft, parent) -> new StackGroupConfigScreen());
    }

    private static void onScreenOpening(ScreenEvent.Opening event) {
        if (EmiPlusPlusConfig.emiOnlyInRecipeBook) {
            EmiConfig.enabled = false;
        }
    }
}