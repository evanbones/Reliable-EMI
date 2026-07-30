package com.evandev.remi;

import com.evandev.remi.config.ReliableEmiConfig;
import dev.emi.emi.config.EmiConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

public class ReliableEmiClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ReliableEmiConfig.load();

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (ReliableEmiConfig.emiOnlyInRecipeBook) {
                EmiConfig.enabled = false;
            }
        });
    }
}
