package com.evandev.emixx.integration.modmenu;

import com.evandev.emixx.feature.stackgroup.gui.StackGroupConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new StackGroupConfigScreen();
    }
}