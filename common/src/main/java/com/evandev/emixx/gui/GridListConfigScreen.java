package com.evandev.emixx.gui;

import com.evandev.EmiPlusPlus;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public abstract class GridListConfigScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private GridList<?> list;
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private final TabNavigationBar tabNavigationBar;
    private final String name;

    protected GridListConfigScreen(String name) {
        super(EmiPlusPlus.text("gui", name));
        this.name = name;
        this.tabNavigationBar = TabNavigationBar.builder(tabManager, 0).addTabs(new PrebuiltTab(name)).build();
    }

    protected abstract GridList<?> createList();
    protected abstract void save();
    protected abstract void reload();

    @Override
    protected void init() {
        list = createList();
        layout.addTitleHeader(title, font);
        layout.addToContents(list);
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> {
            save();
            onClose();
            reload();
        }).width(200).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
        list.add();
    }

    @Override
    protected void repositionElements() {
        list.updateSize(width, layout);
        layout.arrangeElements();
        tabNavigationBar.setWidth(width);
        tabNavigationBar.arrangeElements();
        tabManager.setTabArea(new ScreenRectangle(0, tabNavigationBar.getRectangle().bottom(), width, height));
    }

    private static class PrebuiltTab extends GridLayoutTab {
        public PrebuiltTab(String name) {
            super(EmiPlusPlus.text("gui", name + ".prebuilt"));
        }
    }
}
