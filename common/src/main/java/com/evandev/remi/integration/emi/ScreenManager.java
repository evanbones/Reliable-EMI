package com.evandev.remi.integration.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.remi.feature.creativemodetab.gui.CreativeModeTabGui;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ScreenManager {
    public static final int ENTRY_SIZE = 18;
    public static EmiScreenManager.ScreenSpace indexScreenSpace;
    public static Component customIndexTitle;
    private static Screen screen;

    public static boolean isSearching() {
        return indexScreenSpace != null && indexScreenSpace.search
                && !EmiScreenManager.search.getValue().isEmpty();
    }

    public static void onScreenInitialized(Screen s) {
        screen = s;
    }

    public static void onIndexScreenSpaceCreated(EmiScreenManager.ScreenSpace space) {
        indexScreenSpace = space;
        if (screen == null) return;

        if (ReliableEmiConfig.enableCreativeModeTabs) {
            CreativeModeTabGui.initialize(screen);
            CreativeModeTabManager.initialize();
        }
    }

    public static boolean onMouseScrolled(double mouseX, double mouseY, double amount) {
        if (ReliableEmiConfig.enableCreativeModeTabs && indexScreenSpace != null
                && CreativeModeTabGui.contains(mouseX, mouseY)) {
            return CreativeModeTabGui.onMouseScrolled(amount);
        }
        return false;
    }

    public static void setCustomIndexTitle(Component title) {
        var font = Minecraft.getInstance().font;
        int spaceWidth = ScreenManager.indexScreenSpace != null ? ScreenManager.indexScreenSpace.tw : 0;
        int maxWidth = spaceWidth * ScreenManager.ENTRY_SIZE - 20;

        ScreenManager.customIndexTitle = (maxWidth > 0 && font.width(title) > maxWidth)
                ? Component.literal(font.plainSubstrByWidth(title.getString(), maxWidth - font.width("...")) + "...")
                : title;
    }

    public static void removeCustomIndexTitle(Component component) {
        if (customIndexTitle == component) customIndexTitle = null;
    }
}
