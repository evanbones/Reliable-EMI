package com.evandev.emixx.config;

import com.evandev.EmiPlusPlus;
import com.evandev.emixx.platform.Services;
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

public class EmiPlusPlusConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean enableCreativeModeTabs = true;
    public static boolean syncSelectedCreativeModeTab = true;
    public static boolean showCreativeTabNameInSearchbar = true;
    public static List<String> disabledCreativeModeTabs = new ArrayList<>(List.of("minecraft:op_blocks"));
    public static Map<String, List<String>> stackGroupItemOrder = new HashMap<>();

    public static boolean enableStackGroups = true;
    public static boolean enableCreateStackGroupButton = true;
    public static List<String> disabledStackGroups = new ArrayList<>();

    public static boolean emiOnlyInRecipeBook = false;

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


    public static Path getConfigDir() {
        return Services.PLATFORM.getConfigDirectory().resolve(EmiPlusPlus.MOD_ID);
    }

    public static Path getConfigPath() {
        return getConfigDir().resolve(EmiPlusPlus.MOD_ID + ".json");
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

                    emiOnlyInRecipeBook = data.emiOnlyInRecipeBook;
                    disablePaginationWrapping = data.disablePaginationWrapping;
                    scrollInsteadOfPagination = data.scrollInsteadOfPagination;
                    showTitleInsteadOfPageNumbers = data.showTitleInsteadOfPageNumbers;
                    hidePageButtonWhenOnePage = data.hidePageButtonWhenOnePage;
                    incrementalScrollbarFill = data.incrementalScrollbarFill;
                }
            } catch (IOException | JsonSyntaxException e) {
                EmiPlusPlus.LOGGER.error("[EMI++] Failed to load config", e);
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
            EmiPlusPlus.LOGGER.error("[EMI++] Failed to save config", e);
        }
    }

    private static ConfigData collectData() {
        ConfigData data = new ConfigData();
        data.enableCreativeModeTabs = enableCreativeModeTabs;
        data.syncSelectedCreativeModeTab = syncSelectedCreativeModeTab;
        data.showCreativeTabNameInSearchbar = showCreativeTabNameInSearchbar;
        data.disabledCreativeModeTabs = new ArrayList<>(disabledCreativeModeTabs);
        data.enableStackGroups = enableStackGroups;
        data.enableCreateStackGroupButton = enableCreateStackGroupButton;
        data.disabledStackGroups = new ArrayList<>(disabledStackGroups);
        data.emiOnlyInRecipeBook = emiOnlyInRecipeBook;
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
        return data;
    }

    private static class ConfigData {
        boolean enableCreativeModeTabs = true;
        boolean syncSelectedCreativeModeTab = true;
        boolean showCreativeTabNameInSearchbar = true;
        List<String> disabledCreativeModeTabs = new ArrayList<>(List.of("minecraft:op_blocks"));
        boolean enableStackGroups = true;
        boolean enableCreateStackGroupButton = true;
        List<String> disabledStackGroups = new ArrayList<>();
        Map<String, List<String>> stackGroupItemOrder = new HashMap<>();
        boolean emiOnlyInRecipeBook = false;

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
    }
}