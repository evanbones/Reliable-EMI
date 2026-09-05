package com.evandev.remi.integration.modmenu;

import com.evandev.remi.config.ReliableEmiConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ReliableEmiConfigScreen::createScreen;
    }
}