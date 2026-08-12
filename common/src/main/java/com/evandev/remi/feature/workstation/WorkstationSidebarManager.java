package com.evandev.remi.feature.workstation;

import com.google.common.collect.Sets;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkstationSidebarManager {

    public static SidebarType WORKSTATION;
    public static List<EmiIngredient> workstationStacks = List.of();
    private static AbstractContainerScreen<?> lastWorkstationScreen = null;
    private static long lastWorkstationSync = 0;

    public static void updateWorkstationCraftables() {
        AbstractContainerScreen<?> screen = EmiApi.getHandledScreen();
        int minDelay = 400;
        if (WORKSTATION != null && EmiScreenManager.hasSidebarVisible(WORKSTATION)) {
            minDelay = 50;
        }
        if (screen != lastWorkstationScreen || Math.abs(System.currentTimeMillis() - lastWorkstationSync) >= minDelay) {
            lastWorkstationSync = System.currentTimeMillis();
            lastWorkstationScreen = screen;
            List<EmiIngredient> newWorkstationStacks = getWorkstationCraftables(screen);
            if (!newWorkstationStacks.equals(workstationStacks)) {
                workstationStacks = newWorkstationStacks;
                EmiScreenManager.SidebarPanel searchPanel = EmiScreenManager.getSearchPanel();
                if (searchPanel != null && searchPanel.space != null) {
                    searchPanel.space.batcher.repopulate();
                    if (WORKSTATION != null && searchPanel.getType() == WORKSTATION) {
                        EmiSearch.update();
                    }
                }
                if (WORKSTATION != null) {
                    EmiScreenManager.repopulatePanels(WORKSTATION);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<EmiIngredient> getWorkstationCraftables(AbstractContainerScreen<?> screen) {
        if (screen == null || screen instanceof InventoryScreen) {
            return List.of();
        }
        List<EmiRecipeHandler<?>> handlers = (List) EmiRecipeFiller.getAllHandlers(screen);
        if (handlers.isEmpty()) {
            return List.of();
        }
        Set<EmiRecipe> set = Sets.newHashSet();
        List<EmiRecipe> allRecipes = EmiRecipes.manager.getRecipes();
        for (EmiRecipe r : allRecipes) {
            if (r.hideCraftable() || r.getOutputs().isEmpty()) {
                continue;
            }
            for (EmiRecipeHandler<?> handler : handlers) {
                if (handler.supportsRecipe(r)) {
                    set.add(r);
                    break;
                }
            }
        }

        if (set.isEmpty()) {
            return List.of();
        }

        return set.stream()
                .map(EmiFavorite.Craftable::new)
                .sorted(Comparator.comparingInt((EmiFavorite.Craftable a) -> EmiStackList.getIndex(a.getStack()))
                        .thenComparingLong(EmiFavorite::getAmount))
                .collect(Collectors.toList());
    }
}
