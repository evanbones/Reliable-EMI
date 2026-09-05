package com.evandev.remi;

import com.evandev.remi.config.ReliableEmiConfig;
import net.fabricmc.api.ClientModInitializer;

public class ReliableEmiClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ReliableEmiConfig.load();
    }
}

