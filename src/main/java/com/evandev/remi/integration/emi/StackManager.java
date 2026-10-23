package com.evandev.remi.integration.emi;

import com.evandev.remi.feature.stackgroup.EmiGroupStack;
import com.evandev.remi.feature.stackgroup.StackGroupManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiHidden;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StackManager {
    public static final Map<SidebarType, Set<ResourceLocation>> expandedStackGroups = new ConcurrentHashMap<>();
    private static final Object LOCK = new Object();
    private static final Map<SidebarType, Integer> sidebarVersions = new EnumMap<>(SidebarType.class);
    public static volatile List<EmiStack> indexStacks = EmiStackList.filteredStacks;
    public static volatile List<EmiStack> sourceStacks = List.of();
    public static volatile List<EmiStack> searchedStacks = List.of();
    public static volatile List<EmiStack> displayedStacks = new ArrayList<>();
    public static volatile List<EmiStack> unsearchedStacks = new ArrayList<>();
    public static EmiStack[][] stackGrid = new EmiStack[0][0];
    private static volatile List<EmiStack> groupedStacks = List.of();
    private static volatile List<EmiStack> groupedUnsearchedStacks = List.of();
    private static volatile List<EmiStack> groupedIndexStacks = List.of();
    private static List<EmiStack> lastRepopulatedDisplayedStacks;
    private static List<EmiStack> lastRepopulatedUnsearchedStacks;
    private static final AtomicInteger globalStacksVersion = new AtomicInteger();

    public static int getStacksVersion(SidebarType type) {
        if (type == null) return globalStacksVersion.get();
        return sidebarVersions.getOrDefault(type, 0) + globalStacksVersion.get();
    }

    public static void invalidateStacks() {
        globalStacksVersion.incrementAndGet();
    }

    public static void invalidateStacks(SidebarType type) {
        if (type == null) {
            globalStacksVersion.incrementAndGet();
        } else {
            sidebarVersions.put(type, sidebarVersions.getOrDefault(type, 0) + 1);
        }
    }

    public static boolean isGroupExpanded(SidebarType type, ResourceLocation groupId) {
        if (type == null) return false;
        Set<ResourceLocation> set = expandedStackGroups.get(type);
        return set != null && set.contains(groupId);
    }

    public static void reload() {
        invalidateStacks();
        expandedStackGroups.clear();
        List<EmiStack> index = EmiStackList.filteredStacks;
        StackGroupManager.buildGroupedEmiStacksAndStackGroupToContents(index);
        synchronized (LOCK) {
            indexStacks = index;
            groupedIndexStacks = List.of();
        }
        updateSourceStacks(index);
    }

    public static void repopulateIndexPanelsIfDirty() {
        if (lastRepopulatedDisplayedStacks == displayedStacks && lastRepopulatedUnsearchedStacks == unsearchedStacks) {
            return;
        }
        lastRepopulatedDisplayedStacks = displayedStacks;
        lastRepopulatedUnsearchedStacks = unsearchedStacks;
        EmiScreenManager.repopulatePanels(SidebarType.INDEX);
    }

    public static void updateSourceStacks(List<EmiStack> src) {
        List<EmiStack> searched = filterHidden(src);
        List<EmiStack> grouped = buildGroupedStacks(searched);
        List<EmiStack> displayed = buildDisplayedStacks(grouped);
        synchronized (LOCK) {
            sourceStacks = src;
            searchedStacks = searched;
            groupedStacks = grouped;
            displayedStacks = displayed;
            groupedUnsearchedStacks = grouped;
            unsearchedStacks = displayed;
        }
    }

    public static void search(List<EmiStack> src, String keyword) {
        List<EmiStack> grouped = buildGroupedStacks(filterHidden(src));
        List<EmiStack> displayed = buildDisplayedStacks(grouped);
        synchronized (LOCK) {
            sourceStacks = src;
            groupedUnsearchedStacks = grouped;
            unsearchedStacks = displayed;
        }
        EmiSearch.search(keyword);
    }

    public static void buildStacks(List<EmiStack> searched) {
        List<EmiStack> filtered = filterHidden(searched);
        List<EmiStack> grouped = buildGroupedStacks(filtered);
        List<EmiStack> displayed = buildDisplayedStacks(grouped);
        synchronized (LOCK) {
            searchedStacks = filtered;
            groupedStacks = grouped;
            displayedStacks = displayed;
        }
    }

    private static List<EmiStack> filterHidden(List<EmiStack> stacks) {
        if (EmiConfig.editMode) {
            return stacks;
        }
        List<EmiStack> filtered = new ArrayList<>(stacks.size());
        for (EmiStack s : stacks) {
            if (!EmiHidden.isHidden(s)) filtered.add(s);
        }
        return filtered;
    }

    private static List<EmiStack> buildGroupedStacks(List<EmiStack> stacks) {
        boolean isFullIndex = stacks.size() == indexStacks.size();
        List<EmiStack> grouped;
        if (isFullIndex && !groupedIndexStacks.isEmpty()) {
            grouped = groupedIndexStacks;
        } else {
            grouped = StackGroupManager.buildGroupedStacks(stacks);
            if (isFullIndex) {
                groupedIndexStacks = grouped;
            }
        }

        for (EmiStack s : grouped) {
            if (s instanceof EmiGroupStack gs) {
                gs.isExpanded = isGroupExpanded(SidebarType.INDEX, gs.group.getId());
            }
        }
        return grouped;
    }

    private static List<EmiStack> buildDisplayedStacks(List<EmiStack> grouped) {
        List<EmiStack> result = new ArrayList<>(grouped.size());
        for (EmiStack s : grouped) {
            if (s instanceof EmiGroupStack gs) {
                var items = gs.getItems();
                if (!items.isEmpty()) {
                    if (items.size() == 1) {
                        result.add(items.get(0).realStack);
                    } else if (gs.isExpanded) {
                        result.add(gs);
                        for (var item : items) {
                            result.add(item.realStack);
                        }
                    } else {
                        result.add(gs);
                    }
                }
            } else {
                result.add(s);
            }
        }
        return result;
    }

    public static void onStackInteraction(EmiIngredient ingredient, SidebarType type) {
        if (!(ingredient instanceof EmiGroupStack gs)) return;
        if (type == null) type = SidebarType.INDEX;

        Layout.textureDirty = true;
        Set<ResourceLocation> set = expandedStackGroups.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet());
        boolean isExpanded = !set.contains(gs.group.getId());

        if (isExpanded) {
            set.add(gs.group.getId());
        } else {
            set.remove(gs.group.getId());
        }

        gs.isExpanded = isExpanded;

        if (type == SidebarType.INDEX) {
            synchronized (LOCK) {
                displayedStacks = buildDisplayedStacks(groupedStacks);
                unsearchedStacks = groupedUnsearchedStacks == groupedStacks
                        ? displayedStacks
                        : buildDisplayedStacks(groupedUnsearchedStacks);
            }
        }
        EmiScreenManager.repopulatePanels(type);
        EmiScreenManager.recalculate();
    }
}