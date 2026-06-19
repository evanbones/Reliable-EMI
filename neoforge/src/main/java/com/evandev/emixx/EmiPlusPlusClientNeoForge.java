package com.evandev.emixx;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import dev.emi.emi.config.EmiConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class EmiPlusPlusClientNeoForge {
    public static void register(ModContainer container) {
        EmiPlusPlusConfig.load();

        NeoForge.EVENT_BUS.addListener(EmiPlusPlusClientNeoForge::onScreenOpening);
    }

    private static void onScreenOpening(ScreenEvent.Opening event) {
        if (EmiPlusPlusConfig.emiOnlyInRecipeBook) {
            EmiConfig.enabled = false;
        }
    }
}
