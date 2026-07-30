package com.evandev.remi.feature.stackgroup.gui;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.stackgroup.StackGroupManager;
import com.evandev.remi.gui.GridList;
import com.evandev.remi.gui.GridListConfigScreen;
import com.evandev.remi.integration.emi.StackManager;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
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

    public StackGroupConfigScreen(Screen parent) {
        super("stack_group_config", parent);
        this.disabledStackGroups = new HashSet<>();
        for (String s : ReliableEmiConfig.disabledStackGroups) {
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
        LinearLayout headerContent = LinearLayout.vertical().spacing(4);
        headerContent.addChild(new StringWidget(title, font).alignCenter());

        EditBox searchBox = new EditBox(font, 0, 0, 200, 20, Component.translatable("remi.configuration.search"));
        searchBox.setResponder(s -> {
            searchQuery = s.toLowerCase(Locale.ROOT);
            if (list instanceof StackGroupGridList sgList) {
                sgList.setSearchQuery(searchQuery);
                sgList.refreshList();
            }
        });
        headerContent.addChild(searchBox);
        layout.addToHeader(headerContent);
        layout.setHeaderHeight(50);

        layout.addToContents(list);
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> {
            save();
            onClose();
            reload();
        }).width(200).build());
    }

    @Override
    protected void save() {
        ReliableEmiConfig.disabledStackGroups = new ArrayList<>();
        for (ResourceLocation loc : disabledStackGroups) {
            ReliableEmiConfig.disabledStackGroups.add(loc.toString());
        }
        ReliableEmiConfig.save();
    }

    @Override
    protected void reload() {
        StackGroupManager.reload();
        StackManager.reload();
        EmiScreenManager.recalculate();
    }
}