package com.evandev.remi.feature.creativemodetab;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.gui.CreativeModeTabGui;
import com.evandev.remi.feature.creativemodetab.gui.itemtab.ItemTab;
import com.evandev.remi.integration.emi.ScreenManager;
import com.evandev.remi.integration.emi.StackManager;
import dev.emi.emi.api.stack.EmiIngredient;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CreativeModeTabManager {
    public static final List<CreativeModeTab> HIDDEN_CREATIVE_MODE_TABS;
    private static final List<CreativeModeTab> disabledCreativeModeTabs = new ArrayList<>();
    private static final List<CreativeModeTab> creativeModeTabs = new ArrayList<>();
    private static final Map<CreativeModeTab, ItemTab> tabCache = new IdentityHashMap<>();
    private static final Map<CreativeModeTab, Set<Item>> tabItemCache = new IdentityHashMap<>();
    public static int scrollOffset = 0;
    private static CreativeModeTab currentTab;
    private static boolean isSelectingVanillaByEmiPlusPlus = false;
    private static boolean isSelectingEmiPlusPlusByVanilla = false;
    private static CreativeModeTab indexCreativeModeTab;
    private static Method recreativeIconMethod = null;
    private static boolean checkedRecreativeMethod = false;

    static {
        List<CreativeModeTab> hidden = new ArrayList<>();
        addFromRegistry(hidden, CreativeModeTabs.INVENTORY);
        addFromRegistry(hidden, CreativeModeTabs.HOTBAR);
        addFromRegistry(hidden, CreativeModeTabs.SEARCH);
        HIDDEN_CREATIVE_MODE_TABS = Collections.unmodifiableList(hidden);
    }

    public static CreativeModeTab getCurrentTab() {
        return currentTab;
    }

    public static CreativeModeTab getIndexCreativeModeTab() {
        return indexCreativeModeTab;
    }

    public static Set<Item> getTabItems(CreativeModeTab tab) {
        if (tab == null) return Set.of();
        return tabItemCache.computeIfAbsent(tab, t -> {
            Set<Item> items = new HashSet<>();
            for (ItemStack is : t.getDisplayItems()) {
                if (!is.isEmpty()) {
                    items.add(is.getItem());
                }
            }
            return items;
        });
    }

    public static boolean isIngredientInCurrentTab(EmiIngredient ingredient) {
        if (currentTab == null || currentTab == indexCreativeModeTab) {
            return true;
        }
        if (ingredient == null) return false;
        Set<Item> items = getTabItems(currentTab);
        if (items.isEmpty()) return true;
        for (EmiStack stack : ingredient.getEmiStacks()) {
            if (stack != null && stack.getItemStack() != null && !stack.getItemStack().isEmpty()) {
                if (items.contains(stack.getItemStack().getItem())) {
                    return true;
                }
            }
        }
        return false;
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
        for (String s : ReliableEmiConfig.disabledCreativeModeTabs) {
            CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ResourceLocation.parse(s));
            if (tab == null && s.startsWith("emixx:")) {
                tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ResourceLocation.parse("remi:" + s.substring(6)));
            } else if (tab == null && s.startsWith("remi:")) {
                tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ResourceLocation.parse("emixx:" + s.substring(5)));
            }
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
        tabItemCache.clear();
        StackManager.invalidateStacks();
        scrollOffset = Math.min(scrollOffset, getMaxScroll());
    }

    private static boolean hasRecreativeIcon(CreativeModeTab tab) {
        if (!checkedRecreativeMethod) {
            try {
                recreativeIconMethod = CreativeModeTab.class.getMethod("recreative$getCustomIcon");
            } catch (Exception ignored) {
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

        if (ReliableEmiConfig.showTitleInsteadOfPageNumbers) {
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

        if (!isSelectingEmiPlusPlusByVanilla && ReliableEmiConfig.syncSelectedCreativeModeTab
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

        EmiScreenManager.recalculate();
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
        if (!ReliableEmiConfig.syncSelectedCreativeModeTab) return;
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