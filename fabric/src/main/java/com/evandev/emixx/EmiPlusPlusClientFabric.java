package com.evandev.emixx;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import dev.emi.emi.config.EmiConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

public class EmiPlusPlusClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EmiPlusPlusConfig.load();

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (EmiPlusPlusConfig.emiOnlyInRecipeBook) {
                EmiConfig.enabled = false;
            }
        });
    }
}
