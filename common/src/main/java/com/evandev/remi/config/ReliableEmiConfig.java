package com.evandev.remi.config;

import com.evandev.ReliableEmi;
import com.evandev.remi.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReliableEmiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean enableCreativeModeTabs = true;
    public static boolean syncSelectedCreativeModeTab = true;
    public static boolean showCreativeTabNameInSearchbar = true;
    public static int maxSidebarTabs = 0;
    public static List<String> disabledCreativeModeTabs = new ArrayList<>(List.of("minecraft:op_blocks"));
    public static Map<String, List<String>> stackGroupItemOrder = new HashMap<>();

    public static boolean enableStackGroups = true;
    public static boolean enableCreateStackGroupButton = true;
    public static List<String> disabledStackGroups = new ArrayList<>();

    public static boolean enableCategorizedTagPages = true;
    public static boolean enableEntityTags = true;
    public static boolean enableTagSearchEnhancements = true;

    public static boolean emiOnlyInRecipeBook = false;
    public static boolean emiOnlyInRecipeBookState = false;
    public static boolean dragCheatToInventory = true;

    public static boolean disablePaginationWrapping = false;
    public static boolean scrollInsteadOfPagination = false;
    public static boolean showTitleInsteadOfPageNumbers = false;
    public static boolean hidePageButtonWhenOnePage = false;
    public static boolean incrementalScrollbarFill = false;

    public static boolean searchWidgetAlignWithPanel = false;
    public static int searchWidgetTopOffset = 0;
    public static int searchWidgetHorizontalPadding = 4;
    public static int searchWidgetVerticalPadding = 2;
    public static int searchWidgetSuggestionTextColor = 0xFF808080;
    public static int searchWidgetTextColor = 0xFFFFFFFF;
    public static boolean searchWidgetUseVanillaTexture = false;


    public static Path getConfigDir() {
        Path remiDir = Services.PLATFORM.getConfigDirectory().resolve(ReliableEmi.MOD_ID);
        if (Files.isDirectory(remiDir) || Files.exists(remiDir)) {
            return remiDir;
        }
        Path emixxDir = Services.PLATFORM.getConfigDirectory().resolve("emixx");
        if (Files.isDirectory(emixxDir) || Files.exists(emixxDir)) {
            return emixxDir;
        }
        return remiDir;
    }

    public static Path getConfigPath() {
        Path dir = getConfigDir();
        Path remiPath = dir.resolve(ReliableEmi.MOD_ID + ".json");
        if (Files.exists(remiPath)) {
            return remiPath;
        }
        Path emixxPath = dir.resolve("emixx.json");
        if (Files.exists(emixxPath)) {
            return emixxPath;
        }
        Path rootEmixxPath = Services.PLATFORM.getConfigDirectory().resolve("emixx.json");
        if (Files.exists(rootEmixxPath)) {
            return rootEmixxPath;
        }
        Path rootRemiPath = Services.PLATFORM.getConfigDirectory().resolve(ReliableEmi.MOD_ID + ".json");
        if (Files.exists(rootRemiPath)) {
            return rootRemiPath;
        }
        return remiPath;
    }

    public static void load() {
        Path path = getConfigPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    enableCreativeModeTabs = data.enableCreativeModeTabs;
                    syncSelectedCreativeModeTab = data.syncSelectedCreativeModeTab;
                    showCreativeTabNameInSearchbar = data.showCreativeTabNameInSearchbar;
                    maxSidebarTabs = data.maxSidebarTabs;

                    if (data.disabledCreativeModeTabs != null) {
                        disabledCreativeModeTabs = new ArrayList<>(data.disabledCreativeModeTabs);
                    }

                    enableStackGroups = data.enableStackGroups;
                    enableCreateStackGroupButton = data.enableCreateStackGroupButton;

                    if (data.disabledStackGroups != null) {
                        disabledStackGroups = new ArrayList<>(data.disabledStackGroups);
                    }

                    if (data.stackGroupItemOrder != null) {
                        stackGroupItemOrder = new HashMap<>(data.stackGroupItemOrder);
                    }

                    enableCategorizedTagPages = data.enableCategorizedTagPages;
                    enableEntityTags = data.enableEntityTags;
                    enableTagSearchEnhancements = data.enableTagSearchEnhancements;

                    emiOnlyInRecipeBook = data.emiOnlyInRecipeBook;
                    emiOnlyInRecipeBookState = data.emiOnlyInRecipeBookState;
                    dragCheatToInventory = data.dragCheatToInventory;
                    disablePaginationWrapping = data.disablePaginationWrapping;
                    scrollInsteadOfPagination = data.scrollInsteadOfPagination;
                    showTitleInsteadOfPageNumbers = data.showTitleInsteadOfPageNumbers;
                    hidePageButtonWhenOnePage = data.hidePageButtonWhenOnePage;
                    incrementalScrollbarFill = data.incrementalScrollbarFill;

                    searchWidgetAlignWithPanel = data.searchWidgetAlignWithPanel;
                    searchWidgetTopOffset = data.searchWidgetTopOffset;
                    searchWidgetHorizontalPadding = data.searchWidgetHorizontalPadding;
                    searchWidgetVerticalPadding = data.searchWidgetVerticalPadding;
                    searchWidgetSuggestionTextColor = data.searchWidgetSuggestionTextColor;
                    searchWidgetTextColor = data.searchWidgetTextColor;
                    searchWidgetUseVanillaTexture = data.searchWidgetUseVanillaTexture;
                }
            } catch (IOException | JsonSyntaxException e) {
                ReliableEmi.LOGGER.error("Failed to load config", e);
            }
        }
        save();
    }

    public static void save() {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(collectData(), writer);
            }
        } catch (IOException | JsonSyntaxException e) {
            ReliableEmi.LOGGER.error("Failed to save config", e);
        }
    }

    private static ConfigData collectData() {
        ConfigData data = new ConfigData();
        data.enableCreativeModeTabs = enableCreativeModeTabs;
        data.syncSelectedCreativeModeTab = syncSelectedCreativeModeTab;
        data.showCreativeTabNameInSearchbar = showCreativeTabNameInSearchbar;
        data.maxSidebarTabs = maxSidebarTabs;
        data.disabledCreativeModeTabs = new ArrayList<>(disabledCreativeModeTabs);
        data.enableStackGroups = enableStackGroups;
        data.enableCreateStackGroupButton = enableCreateStackGroupButton;
        data.disabledStackGroups = new ArrayList<>(disabledStackGroups);
        data.enableCategorizedTagPages = enableCategorizedTagPages;
        data.enableEntityTags = enableEntityTags;
        data.enableTagSearchEnhancements = enableTagSearchEnhancements;
        data.emiOnlyInRecipeBook = emiOnlyInRecipeBook;
        data.emiOnlyInRecipeBookState = emiOnlyInRecipeBookState;
        data.dragCheatToInventory = dragCheatToInventory;
        data.stackGroupItemOrder = new HashMap<>(stackGroupItemOrder);
        data.disablePaginationWrapping = disablePaginationWrapping;
        data.scrollInsteadOfPagination = scrollInsteadOfPagination;
        data.showTitleInsteadOfPageNumbers = showTitleInsteadOfPageNumbers;
        data.hidePageButtonWhenOnePage = hidePageButtonWhenOnePage;
        data.incrementalScrollbarFill = incrementalScrollbarFill;
        data.searchWidgetAlignWithPanel = searchWidgetAlignWithPanel;
        data.searchWidgetTopOffset = searchWidgetTopOffset;
        data.searchWidgetHorizontalPadding = searchWidgetHorizontalPadding;
        data.searchWidgetVerticalPadding = searchWidgetVerticalPadding;
        data.searchWidgetSuggestionTextColor = searchWidgetSuggestionTextColor;
        data.searchWidgetTextColor = searchWidgetTextColor;
        data.searchWidgetUseVanillaTexture = searchWidgetUseVanillaTexture;
        return data;
    }

    private static class ConfigData {
        boolean enableCreativeModeTabs = true;
        boolean syncSelectedCreativeModeTab = true;
        boolean showCreativeTabNameInSearchbar = true;
        int maxSidebarTabs = 0;
        List<String> disabledCreativeModeTabs = new ArrayList<>(List.of("minecraft:op_blocks"));
        boolean enableStackGroups = true;
        boolean enableCreateStackGroupButton = true;
        List<String> disabledStackGroups = new ArrayList<>();
        Map<String, List<String>> stackGroupItemOrder = new HashMap<>();
        boolean emiOnlyInRecipeBook = false;
        boolean emiOnlyInRecipeBookState = false;
        boolean dragCheatToInventory = true;

        // Tags
        boolean enableCategorizedTagPages = true;
        boolean enableEntityTags = true;
        boolean enableTagSearchEnhancements = true;

        // Scrolling
        boolean disablePaginationWrapping = false;
        boolean scrollInsteadOfPagination = false;
        boolean showTitleInsteadOfPageNumbers = false;
        boolean hidePageButtonWhenOnePage = false;
        boolean incrementalScrollbarFill = false;

        // Search Widget
        boolean searchWidgetAlignWithPanel = false;
        int searchWidgetTopOffset = 0;
        int searchWidgetHorizontalPadding = 4;
        int searchWidgetVerticalPadding = 2;
        int searchWidgetSuggestionTextColor = 0xFF808080;
        int searchWidgetTextColor = 0xFFFFFFFF;
        boolean searchWidgetUseVanillaTexture = false;
    }
}