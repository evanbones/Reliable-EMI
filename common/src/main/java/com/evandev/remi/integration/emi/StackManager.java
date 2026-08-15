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

public class StackManager {
    public static final Map<SidebarType, Set<ResourceLocation>> expandedStackGroups = new HashMap<>();
    public static List<EmiStack> indexStacks = EmiStackList.filteredStacks;
    public static List<EmiStack> sourceStacks = List.of();
    public static List<EmiStack> searchedStacks = List.of();
    public static List<EmiStack> displayedStacks = new ArrayList<>();
    public static List<EmiStack> unsearchedStacks = new ArrayList<>();
    public static EmiStack[][] stackGrid = new EmiStack[0][0];
    private static List<EmiStack> groupedStacks = List.of();
    private static List<EmiStack> groupedUnsearchedStacks = List.of();
    private static List<EmiStack> groupedIndexStacks = List.of();
    private static List<EmiStack> lastRepopulatedDisplayedStacks;
    private static List<EmiStack> lastRepopulatedUnsearchedStacks;

    public static boolean isGroupExpanded(SidebarType type, ResourceLocation groupId) {
        if (type == null) return false;
        Set<ResourceLocation> set = expandedStackGroups.get(type);
        return set != null && set.contains(groupId);
    }

    public static void reload() {
        expandedStackGroups.clear();
        indexStacks = EmiStackList.filteredStacks;
        groupedIndexStacks = List.of();
        StackGroupManager.buildGroupedEmiStacksAndStackGroupToContents(indexStacks);
        updateSourceStacks(indexStacks);
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
        sourceStacks = src;
        buildStacks(src);
        groupedUnsearchedStacks = groupedStacks;
        unsearchedStacks = displayedStacks;
    }

    public static void search(List<EmiStack> src, String keyword) {
        sourceStacks = src;
        groupedUnsearchedStacks = buildGroupedStacks(filterHidden(src));
        unsearchedStacks = buildDisplayedStacks(groupedUnsearchedStacks);
        EmiSearch.search(keyword);
    }

    public static void buildStacks(List<EmiStack> searched) {
        searchedStacks = filterHidden(searched);
        groupedStacks = buildGroupedStacks(searchedStacks);
        displayedStacks = buildDisplayedStacks(groupedStacks);
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
                if (items.size() == 1) {
                    result.add(items.getFirst().realStack);
                } else if (gs.isExpanded) {
                    result.add(gs);
                    for (var item : items) result.add(item.realStack);
                } else {
                    result.add(gs);
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
        Set<ResourceLocation> set = expandedStackGroups.computeIfAbsent(type, k -> new HashSet<>());
        boolean isExpanded = !set.contains(gs.group.getId());

        if (isExpanded) {
            set.add(gs.group.getId());
        } else {
            set.remove(gs.group.getId());
        }

        gs.isExpanded = isExpanded;

        if (type == SidebarType.INDEX) {
            displayedStacks = buildDisplayedStacks(groupedStacks);
            unsearchedStacks = groupedUnsearchedStacks == groupedStacks
                    ? displayedStacks
                    : buildDisplayedStacks(groupedUnsearchedStacks);
        }
        EmiScreenManager.repopulatePanels(type);
        EmiScreenManager.recalculate();
    }
}