package com.evandev.remi.feature.creativemodetab.gui.itemtab;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record ItemTab(CreativeModeTab creativeModeTab) implements Tab {

    @Override
    public @NotNull Component getTabTitle() {
        return creativeModeTab != null ? creativeModeTab.getDisplayName() : Component.empty();
    }

    @Override
    public void visitChildren(@NotNull Consumer<AbstractWidget> consumer) {
    }

    @Override
    public void doLayout(@NotNull ScreenRectangle rectangle) {
    }
}
