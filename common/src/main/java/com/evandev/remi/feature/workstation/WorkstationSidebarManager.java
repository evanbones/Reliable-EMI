package com.evandev.remi.feature.workstation;

import com.google.common.collect.Sets;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WorkstationSidebarManager {

    private static final Map<MenuType<?>, List<Supplier<EmiRecipeCategory>>> MENU_CATEGORIES = new HashMap<>();
    private static final Map<MenuType<?>, List<Supplier<EmiStack>>> MENU_EXPLICIT_STACKS = new HashMap<>();
    public static SidebarType WORKSTATION;
    public static List<EmiIngredient> workstationStacks = List.of();
    private static AbstractContainerScreen<?> lastWorkstationScreen = null;
    private static long lastWorkstationSync = 0;

    static {
        registerMenuCategory(MenuType.SMITHING, () -> VanillaEmiRecipeCategories.SMITHING);
        registerMenuCategory(MenuType.BREWING_STAND, () -> VanillaEmiRecipeCategories.BREWING);
        registerMenuCategory(MenuType.ANVIL, () -> VanillaEmiRecipeCategories.ANVIL_REPAIRING);
        registerMenuCategory(MenuType.GRINDSTONE, () -> VanillaEmiRecipeCategories.GRINDING);
        registerMenuCategory(MenuType.STONECUTTER, () -> VanillaEmiRecipeCategories.STONECUTTING);
        registerMenuCategory(MenuType.BLAST_FURNACE, () -> VanillaEmiRecipeCategories.BLASTING);
        registerMenuCategory(MenuType.SMOKER, () -> VanillaEmiRecipeCategories.SMOKING);
        registerMenuCategory(MenuType.FURNACE, () -> VanillaEmiRecipeCategories.SMELTING);
        registerMenuCategory(MenuType.CRAFTING, () -> VanillaEmiRecipeCategories.CRAFTING);

        registerMenuStacks(MenuType.SMITHING, () -> EmiStack.of(Items.SMITHING_TABLE));
        registerMenuStacks(MenuType.BREWING_STAND, () -> EmiStack.of(Items.BREWING_STAND));
        registerMenuStacks(MenuType.ANVIL, () -> EmiStack.of(Items.ANVIL), () -> EmiStack.of(Items.CHIPPED_ANVIL), () -> EmiStack.of(Items.DAMAGED_ANVIL));
        registerMenuStacks(MenuType.GRINDSTONE, () -> EmiStack.of(Items.GRINDSTONE));
        registerMenuStacks(MenuType.STONECUTTER, () -> EmiStack.of(Items.STONECUTTER));
        registerMenuStacks(MenuType.BLAST_FURNACE, () -> EmiStack.of(Items.BLAST_FURNACE));
        registerMenuStacks(MenuType.SMOKER, () -> EmiStack.of(Items.SMOKER));
        registerMenuStacks(MenuType.FURNACE, () -> EmiStack.of(Items.FURNACE));
        registerMenuStacks(MenuType.CRAFTING, () -> EmiStack.of(Items.CRAFTING_TABLE));
        registerMenuStacks(MenuType.LOOM, () -> EmiStack.of(Items.LOOM));
        registerMenuStacks(MenuType.CARTOGRAPHY_TABLE, () -> EmiStack.of(Items.CARTOGRAPHY_TABLE));
        registerMenuStacks(MenuType.ENCHANTMENT, () -> EmiStack.of(Items.ENCHANTING_TABLE));
    }

    @SafeVarargs
    public static void registerMenuCategory(MenuType<?> menuType, Supplier<EmiRecipeCategory>... categories) {
        MENU_CATEGORIES.computeIfAbsent(menuType, k -> new ArrayList<>()).addAll(Arrays.asList(categories));
    }

    @SafeVarargs
    public static void registerMenuStacks(MenuType<?> menuType, Supplier<EmiStack>... stacks) {
        MENU_EXPLICIT_STACKS.computeIfAbsent(menuType, k -> new ArrayList<>()).addAll(Arrays.asList(stacks));
    }

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
        if (screen == null) {
            return List.of();
        }

        Set<EmiRecipe> set = Sets.newHashSet();

        List<EmiRecipeHandler<?>> handlers = (List) EmiRecipeFiller.getAllHandlers(screen);
        if (!handlers.isEmpty()) {
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
        }

        List<EmiStack> workstationStacks = getWorkstationStacksForScreen(screen);
        for (EmiStack ws : workstationStacks) {
            List<EmiRecipe> recipes = EmiRecipes.byWorkstation.get(ws);
            if (recipes != null) {
                for (EmiRecipe r : recipes) {
                    if (!r.hideCraftable() && !r.getOutputs().isEmpty()) {
                        set.add(r);
                    }
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

    private static List<EmiStack> getWorkstationStacksForScreen(AbstractContainerScreen<?> screen) {
        if (screen == null) {
            return List.of();
        }

        AbstractContainerMenu menu = screen.getMenu();

        List<EmiStack> stacks = new ArrayList<>();

        try {
            MenuType<?> menuType = menu.getType();
            List<Supplier<EmiRecipeCategory>> categorySuppliers = MENU_CATEGORIES.get(menuType);
            if (categorySuppliers != null) {
                for (Supplier<EmiRecipeCategory> supplier : categorySuppliers) {
                    EmiRecipeCategory category = supplier.get();
                    if (category != null) {
                        for (EmiIngredient ingredient : EmiApi.getRecipeManager().getWorkstations(category)) {
                            stacks.addAll(ingredient.getEmiStacks());
                        }
                    }
                }
            }

            List<Supplier<EmiStack>> explicitSuppliers = MENU_EXPLICIT_STACKS.get(menuType);
            if (explicitSuppliers != null) {
                for (Supplier<EmiStack> supplier : explicitSuppliers) {
                    EmiStack stack = supplier.get();
                    if (stack != null && !stack.isEmpty()) {
                        stacks.add(stack);
                    }
                }
            }
        } catch (UnsupportedOperationException ignored) {
        }

        return stacks;
    }
}
