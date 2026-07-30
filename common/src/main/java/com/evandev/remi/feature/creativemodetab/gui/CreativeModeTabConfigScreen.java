package com.evandev.remi.feature.creativemodetab.gui;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.remi.gui.GridList;
import com.evandev.remi.gui.GridListConfigScreen;
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
        for (String s : ReliableEmiConfig.disabledCreativeModeTabs) {
            disabledCreativeModeTabs.add(ResourceLocation.parse(s));
        }
    }

    @Override
    protected GridList<?> createList() {
        return new CreativeModeTabGridList(this, disabledCreativeModeTabs);
    }

    @Override
    protected void save() {
        ReliableEmiConfig.disabledCreativeModeTabs = new ArrayList<>();
        for (ResourceLocation loc : disabledCreativeModeTabs) {
            ReliableEmiConfig.disabledCreativeModeTabs.add(loc.toString());
        }
        ReliableEmiConfig.save();
    }

    @Override
    protected void reload() {
        CreativeModeTabManager.reload();
    }
}
