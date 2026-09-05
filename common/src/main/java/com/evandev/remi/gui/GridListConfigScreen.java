package com.evandev.remi.gui;

import com.evandev.ReliableEmi;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public abstract class GridListConfigScreen extends Screen {
    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private final TabNavigationBar tabNavigationBar;
    private final Screen parent;
    protected GridList<?> list;

    protected GridListConfigScreen(String name, Screen parent) {
        super(ReliableEmi.text("gui", name));
        this.parent = parent;
        this.tabNavigationBar = TabNavigationBar.builder(tabManager, 0).addTabs(new PrebuiltTab(name)).build();
    }

    protected abstract GridList<?> createList();

    protected abstract void save();

    protected abstract void reload();

    protected void buildLayout() {
        layout.addToHeader(new StringWidget(title, font));
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> {
            save();
            onClose();
            reload();
        }).width(200).build());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        list = createList();
        buildLayout();
        layout.visitWidgets(this::addRenderableWidget);
        addRenderableWidget(list);
        repositionElements();
        list.add();
    }

    @Override
    protected void repositionElements() {
        list.updateSize(width, height, layout.getHeaderHeight(), height - layout.getFooterHeight());
        layout.arrangeElements();
        tabNavigationBar.setWidth(width);
        tabNavigationBar.arrangeElements();
        tabManager.setTabArea(new ScreenRectangle(0, tabNavigationBar.getRectangle().bottom(), width, height));
    }

    private static class PrebuiltTab extends GridLayoutTab {
        public PrebuiltTab(String name) {
            super(ReliableEmi.text("gui", name + ".prebuilt"));
        }
    }
}