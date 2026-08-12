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
    public static EmiStack[][] stackGrid = new EmiStack[0][0];
    private static List<EmiStack> groupedStacks = List.of();
    private static List<EmiStack> groupedIndexStacks = List.of();

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

    public static void updateSourceStacks(List<EmiStack> src) {
        sourceStacks = src;
        buildStacks(src);
    }

    public static void search(List<EmiStack> src, String keyword) {
        sourceStacks = src;
        EmiSearch.search(keyword);
    }

    public static void buildStacks(List<EmiStack> searched) {
        if (EmiConfig.editMode) {
            searchedStacks = searched;
        } else {
            List<EmiStack> filtered = new ArrayList<>(searched.size());
            for (EmiStack s : searched) {
                if (!EmiHidden.isHidden(s)) filtered.add(s);
            }
            searchedStacks = filtered;
        }
        buildGroupedStacks();
        buildDisplayedStacks();
    }

    private static void buildGroupedStacks() {
        boolean isFullIndex = searchedStacks.size() == indexStacks.size();
        if (isFullIndex && !groupedIndexStacks.isEmpty()) {
            groupedStacks = groupedIndexStacks;
        } else {
            groupedStacks = StackGroupManager.buildGroupedStacks(searchedStacks);
            if (isFullIndex) {
                groupedIndexStacks = groupedStacks;
            }
        }

        for (EmiStack s : groupedStacks) {
            if (s instanceof EmiGroupStack gs) {
                gs.isExpanded = isGroupExpanded(SidebarType.INDEX, gs.group.getId());
            }
        }
    }

    private static void buildDisplayedStacks() {
        List<EmiStack> result = new ArrayList<>(groupedStacks.size());
        for (EmiStack s : groupedStacks) {
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
        displayedStacks = result;
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
            buildDisplayedStacks();
        }
        EmiScreenManager.recalculate();
    }
}