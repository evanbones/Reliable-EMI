package com.evandev.emixx.feature.stackgroup.gui;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.emixx.feature.stackgroup.StackGroupManager;
import com.evandev.emixx.gui.GridList;
import com.evandev.emixx.gui.GridListConfigScreen;
import com.evandev.emixx.integration.emi.StackManager;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class StackGroupConfigScreen extends GridListConfigScreen {
    private final Set<ResourceLocation> disabledStackGroups;

    public StackGroupConfigScreen() {
        super("stack_group_config");
        this.disabledStackGroups = new HashSet<>();
        for (String s : EmiPlusPlusConfig.disabledStackGroups) {
            disabledStackGroups.add(ResourceLocation.parse(s));
        }
    }

    @Override
    protected GridList<?> createList() {
        return new StackGroupGridList(this, disabledStackGroups);
    }

    @Override
    protected void save() {
        EmiPlusPlusConfig.disabledStackGroups = new ArrayList<>();
        for (ResourceLocation loc : disabledStackGroups) {
            EmiPlusPlusConfig.disabledStackGroups.add(loc.toString());
        }
        EmiPlusPlusConfig.save();
    }

    @Override
    protected void reload() {
        StackGroupManager.reload();
        StackManager.reload();
        EmiScreenManager.recalculate();
    }
}