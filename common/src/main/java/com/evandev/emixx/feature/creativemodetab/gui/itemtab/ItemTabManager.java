package com.evandev.emixx.feature.creativemodetab.gui.itemtab;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.TabManager;

import java.util.function.Consumer;

public class ItemTabManager extends TabManager {
    private Consumer<ItemTab> onTabSelectedListener;

    public ItemTabManager(Consumer<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        super(addWidget, removeWidget);
    }

    public void setOnTabSelectedListener(Consumer<ItemTab> listener) {
        this.onTabSelectedListener = listener;
    }

    public void onTabSelected(ItemTab tab) {
        if (onTabSelectedListener != null) onTabSelectedListener.accept(tab);
    }
}
