package com.evandev.emixx.feature.creativemodetab;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.emixx.feature.creativemodetab.gui.CreativeModeTabGui;
import com.evandev.emixx.feature.creativemodetab.gui.itemtab.ItemTab;
import com.evandev.emixx.integration.emi.ScreenManager;
import com.evandev.emixx.integration.emi.StackManager;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.*;
import java.util.stream.Collectors;

public class CreativeModeTabManager {
    public static final List<CreativeModeTab> HIDDEN_CREATIVE_MODE_TABS;
    private static final List<CreativeModeTab> disabledCreativeModeTabs = new ArrayList<>();
    private static final List<CreativeModeTab> creativeModeTabs = new ArrayList<>();
    private static final Map<CreativeModeTab, ItemTab> tabCache = new IdentityHashMap<>();

    public static int scrollOffset = 0;
    private static CreativeModeTab currentTab;
    private static boolean isSelectingVanillaByEmiPlusPlus = false;
    private static boolean isSelectingEmiPlusPlusByVanilla = false;

    private static CreativeModeTab indexCreativeModeTab;
    private static java.lang.reflect.Method recreativeIconMethod = null;
    private static boolean checkedRecreativeMethod = false;

    static {
        List<CreativeModeTab> hidden = new ArrayList<>();
        addFromRegistry(hidden, CreativeModeTabs.INVENTORY);
        addFromRegistry(hidden, CreativeModeTabs.HOTBAR);
        addFromRegistry(hidden, CreativeModeTabs.SEARCH);
        HIDDEN_CREATIVE_MODE_TABS = Collections.unmodifiableList(hidden);
    }

    private static void addFromRegistry(List<CreativeModeTab> list, ResourceKey<CreativeModeTab> key) {
        CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(key);
        if (tab != null) list.add(tab);
    }

    public static int getMaxScroll() {
        return Math.max(0, creativeModeTabs.size() - CreativeModeTabGui.tabCount);
    }

    public static List<CreativeModeTab> loadDisabledTabs() {
        List<CreativeModeTab> result = new ArrayList<>();
        for (String s : EmiPlusPlusConfig.disabledCreativeModeTabs) {
            CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ResourceLocation.parse(s));
            if (tab != null) result.add(tab);
        }
        return result;
    }

    public static List<CreativeModeTab> getVisibleCreativeModeTabs() {
        List<CreativeModeTab> tabs = new ArrayList<>(CreativeModeTabs.tabs());
        tabs.removeIf(CreativeModeTabManager::shouldHideTab);
        if (indexCreativeModeTab != null) tabs.addFirst(indexCreativeModeTab);
        return tabs;
    }

    public static void initialize() {
        indexCreativeModeTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH);
        scrollOffset = 0;
        List<ItemTab> page = updateTabs();
        if (page.isEmpty()) return;

        if (currentTab == null) {
            CreativeModeTabGui.selectTab(0, false);
            var bar = CreativeModeTabGui.currentTheme() == CreativeModeTabGui.TabTheme.DEFAULT
                    ? CreativeModeTabGui.topTabNavigationBar : CreativeModeTabGui.leftTabNavigationBar;
            if (!bar.visibleTabs.isEmpty()) onTabSelected(bar.visibleTabs.getFirst());
        }
    }

    public static void reload() {
        if (indexCreativeModeTab == null) {
            indexCreativeModeTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH);
        }

        disabledCreativeModeTabs.clear();
        disabledCreativeModeTabs.addAll(loadDisabledTabs());
        creativeModeTabs.clear();
        creativeModeTabs.addAll(getVisibleCreativeModeTabs());
        tabCache.clear();
        scrollOffset = Math.min(scrollOffset, getMaxScroll());
    }

    private static boolean hasRecreativeIcon(CreativeModeTab tab) {
        if (!checkedRecreativeMethod) {
            try {
                recreativeIconMethod = CreativeModeTab.class.getMethod("recreative$getCustomIcon");
            } catch (Exception e) {
                // Ignore
            }
            checkedRecreativeMethod = true;
        }
        if (recreativeIconMethod != null) {
            try {
                return recreativeIconMethod.invoke(tab) != null;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static boolean shouldHideTab(CreativeModeTab tab) {
        return (tab.getDisplayItems().isEmpty() && !hasRecreativeIcon(tab))
                || HIDDEN_CREATIVE_MODE_TABS.contains(tab)
                || disabledCreativeModeTabs.contains(tab);
    }

    public static void nextPage() {
        if (scrollOffset < getMaxScroll()) scrollOffset++;
        updateTabs();
    }

    public static void previousPage() {
        if (scrollOffset > 0) scrollOffset--;
        updateTabs();
    }

    public static void onTabSelected(ItemTab tab) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread()) {
            mc.execute(() -> onTabSelected(tab));
            return;
        }
        CreativeModeTab selectedTab = tab.creativeModeTab();
        if (selectedTab == null || selectedTab == currentTab) return;
        currentTab = selectedTab;

        if (EmiPlusPlusConfig.showTitleInsteadOfPageNumbers) {
            Component title = tab.creativeModeTab().getDisplayName();
            ScreenManager.setCustomIndexTitle(title);
        }

        var bar = CreativeModeTabGui.currentTheme() == CreativeModeTabGui.TabTheme.DEFAULT
                ? CreativeModeTabGui.topTabNavigationBar : CreativeModeTabGui.leftTabNavigationBar;
        bar.tabButtons.forEach(b -> b.setFocused(false));
        for (var btn : bar.tabButtons) {
            if (btn.tab() instanceof ItemTab(CreativeModeTab creativeModeTab) && creativeModeTab == currentTab) {
                bar.setFocusedChild(btn);
                break;
            }
        }

        if (!isSelectingEmiPlusPlusByVanilla && EmiPlusPlusConfig.syncSelectedCreativeModeTab
                && mc.screen instanceof CreativeModeInventoryScreen cms) {
            isSelectingVanillaByEmiPlusPlus = true;
            cms.selectTab(tab.creativeModeTab());
            cms.searchBox.setCanLoseFocus(true);
            cms.searchBox.setFocused(false);
            isSelectingVanillaByEmiPlusPlus = false;
        }

        List<EmiStack> sourceStacks = (tab.creativeModeTab() == indexCreativeModeTab)
                ? StackManager.indexStacks
                : tab.creativeModeTab().getDisplayItems().stream()
                .filter(itemStack -> !itemStack.isEmpty())
                .map(EmiStack::of)
                .collect(Collectors.toList());

        if (ScreenManager.isSearching()) {
            StackManager.search(sourceStacks, EmiScreenManager.search.getValue());
        } else {
            StackManager.updateSourceStacks(sourceStacks);
        }
    }

    private static List<ItemTab> updateTabs() {
        int count = CreativeModeTabGui.tabCount;
        List<ItemTab> pageTabs = creativeModeTabs.stream()
                .skip(scrollOffset).limit(count)
                .map(tab -> tabCache.computeIfAbsent(tab, ItemTab::new))
                .collect(Collectors.toList());

        var bar = CreativeModeTabGui.currentTheme() == CreativeModeTabGui.TabTheme.VANILLA
                ? CreativeModeTabGui.leftTabNavigationBar : CreativeModeTabGui.topTabNavigationBar;
        bar.setTabs(pageTabs);

        bar.tabButtons.forEach(b -> b.setFocused(false));
        for (var btn : bar.tabButtons) {
            if (btn.tab() instanceof ItemTab(
                    CreativeModeTab creativeModeTab
            ) && creativeModeTab == currentTab && currentTab != null) {
                bar.setFocusedChild(btn);
                break;
            }
        }
        return pageTabs;
    }

    public static void onCreativeModeInventoryScreenTabSelected(CreativeModeTab tab) {
        if (CreativeModeTabGui.tabCount == 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof CreativeModeInventoryScreen cms)) return;
        if (!EmiPlusPlusConfig.syncSelectedCreativeModeTab) return;
        if (isSelectingVanillaByEmiPlusPlus) return;

        CreativeModeTab notHiddenTab = tab;
        if (shouldHideTab(tab) && indexCreativeModeTab != null) notHiddenTab = indexCreativeModeTab;

        int tabIndex = creativeModeTabs.indexOf(notHiddenTab);
        if (tabIndex == -1) return;

        if (tabIndex < scrollOffset) {
            scrollOffset = tabIndex;
        } else if (tabIndex >= scrollOffset + CreativeModeTabGui.tabCount) {
            scrollOffset = tabIndex - CreativeModeTabGui.tabCount + 1;
        }

        List<ItemTab> page = updateTabs();
        int localIndex = tabIndex - scrollOffset;
        if (localIndex < 0 || localIndex >= page.size()) return;

        CreativeModeTabGui.selectTab(localIndex, false);
        isSelectingEmiPlusPlusByVanilla = true;
        onTabSelected(page.get(localIndex));
        cms.searchBox.setCanLoseFocus(true);
        cms.searchBox.setFocused(false);
        isSelectingEmiPlusPlusByVanilla = false;
    }
}