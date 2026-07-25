package com.evandev.emixx.feature.creativemodetab.gui;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.emixx.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.emixx.gui.GridList;
import com.evandev.emixx.gui.GridListConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CreativeModeTabConfigScreen extends GridListConfigScreen {
    private final Set<ResourceLocation> disabledCreativeModeTabs;

    public CreativeModeTabConfigScreen(Screen parent) {
        super("creative_mode_tab_config", parent);
        this.disabledCreativeModeTabs = new HashSet<>();
        for (String s : EmiPlusPlusConfig.disabledCreativeModeTabs) {
            disabledCreativeModeTabs.add(ResourceLocation.parse(s));
        }
    }

    @Override
    protected GridList<?> createList() {
        return new CreativeModeTabGridList(this, disabledCreativeModeTabs);
    }

    @Override
    protected void save() {
        EmiPlusPlusConfig.disabledCreativeModeTabs = new ArrayList<>();
        for (ResourceLocation loc : disabledCreativeModeTabs) {
            EmiPlusPlusConfig.disabledCreativeModeTabs.add(loc.toString());
        }
        EmiPlusPlusConfig.save();
    }

    @Override
    protected void reload() {
        CreativeModeTabManager.reload();
    }
}
