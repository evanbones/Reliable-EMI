package com.evandev.emixx.feature.stackgroup.gui;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.emixx.feature.stackgroup.StackGroupManager;
import com.evandev.emixx.gui.GridList;
import com.evandev.emixx.gui.GridListConfigScreen;
import com.evandev.emixx.integration.emi.StackManager;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class StackGroupConfigScreen extends GridListConfigScreen {
    private final Set<ResourceLocation> disabledStackGroups;
    private String searchQuery = "";

    public StackGroupConfigScreen() {
        super("stack_group_config");
        this.disabledStackGroups = new HashSet<>();
        for (String s : EmiPlusPlusConfig.disabledStackGroups) {
            disabledStackGroups.add(ResourceLocation.parse(s));
        }
    }

    @Override
    protected GridList<?> createList() {
        StackGroupGridList sgList = new StackGroupGridList(this, disabledStackGroups);
        sgList.setSearchQuery(searchQuery);
        return sgList;
    }

    @Override
    protected void buildLayout() {
        layout.addTitleHeader(title, font);

        EditBox searchBox = new EditBox(font, 0, 0, 200, 20, Component.translatable("emixx.configuration.search"));
        searchBox.setResponder(s -> {
            searchQuery = s.toLowerCase(Locale.ROOT);
            if (list instanceof StackGroupGridList sgList) {
                sgList.setSearchQuery(searchQuery);
                sgList.refreshList();
            }
        });
        layout.addToHeader(searchBox);

        layout.addToContents(list);
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> {
            save();
            onClose();
            reload();
        }).width(200).build());
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