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
import dev.emi.emi.runtime.EmiReloadManager;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.function.Supplier;

public class WorkstationSidebarManager {

    private static final Map<MenuType<?>, List<Supplier<EmiRecipeCategory>>> MENU_CATEGORIES = new HashMap<>();
    private static final Map<MenuType<?>, List<Supplier<EmiStack>>> MENU_EXPLICIT_STACKS = new HashMap<>();
    private static final Map<Object, List<EmiIngredient>> CRAFTABLE_CACHE = new HashMap<>();
    public static SidebarType WORKSTATION;
    public static List<EmiIngredient> workstationStacks = List.of();
    private static AbstractContainerScreen<?> lastWorkstationScreen = null;
    private static boolean hasLastWorkstationScreen = false;

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

    public static void reload() {
        CRAFTABLE_CACHE.clear();
        lastWorkstationScreen = null;
        hasLastWorkstationScreen = false;
        workstationStacks = List.of();
    }

    public static void updateWorkstationCraftables() {
        AbstractContainerScreen<?> screen = EmiApi.getHandledScreen();
        if (hasLastWorkstationScreen && screen == lastWorkstationScreen) {
            return;
        }
        if (!EmiReloadManager.isLoaded()) {
            return;
        }
        lastWorkstationScreen = screen;
        hasLastWorkstationScreen = true;

        List<EmiIngredient> newWorkstationStacks = getCachedWorkstationCraftables(screen);
        if (newWorkstationStacks == workstationStacks) {
            return;
        }
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

    private static List<EmiIngredient> getCachedWorkstationCraftables(AbstractContainerScreen<?> screen) {
        if (screen == null) {
            return List.of();
        }
        MenuType<?> menuType = safeGetMenuType(screen.getMenu());
        Object key = menuType != null ? menuType : screen.getClass();
        return CRAFTABLE_CACHE.computeIfAbsent(key, k -> getWorkstationCraftables(screen));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<EmiIngredient> getWorkstationCraftables(AbstractContainerScreen<?> screen) {
        if (screen == null) {
            return List.of();
        }

        Set<EmiRecipe> set = Sets.newHashSet();

        List<EmiRecipeHandler<?>> handlers = (List) EmiRecipeFiller.getAllHandlers(screen);
        if (!handlers.isEmpty()) {
            for (EmiRecipe r : getCandidateRecipesForScreen(screen)) {
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

        List<Sorted> sorted = new ArrayList<>(set.size());
        for (EmiRecipe recipe : set) {
            EmiFavorite.Craftable craftable = new EmiFavorite.Craftable(recipe);
            sorted.add(new Sorted(craftable, EmiStackList.getIndex(craftable.getStack())));
        }
        sorted.sort(Comparator.comparingInt(Sorted::index)
                .thenComparingLong(s -> s.craftable().getAmount()));

        List<EmiIngredient> craftables = new ArrayList<>(sorted.size());
        for (Sorted s : sorted) {
            craftables.add(s.craftable());
        }
        return craftables;
    }

    private static Collection<EmiRecipe> getCandidateRecipesForScreen(AbstractContainerScreen<?> screen) {
        List<EmiRecipeCategory> categories = getRelevantCategoriesForScreen(screen);
        if (categories.isEmpty()) {
            return EmiRecipes.manager.getRecipes();
        }
        if (categories.size() == 1) {
            return EmiRecipes.manager.getRecipes(categories.get(0));
        }

        Set<EmiRecipe> candidates = Sets.newLinkedHashSet();
        for (EmiRecipeCategory category : categories) {
            candidates.addAll(EmiRecipes.manager.getRecipes(category));
        }
        return candidates;
    }

    private static List<EmiRecipeCategory> getRelevantCategoriesForScreen(AbstractContainerScreen<?> screen) {
        MenuType<?> menuType = screen instanceof InventoryScreen ? MenuType.CRAFTING : safeGetMenuType(screen.getMenu());
        if (menuType == null) {
            return List.of();
        }

        List<Supplier<EmiRecipeCategory>> categorySuppliers = MENU_CATEGORIES.get(menuType);
        if (categorySuppliers == null) {
            return List.of();
        }

        List<EmiRecipeCategory> categories = new ArrayList<>();
        for (Supplier<EmiRecipeCategory> supplier : categorySuppliers) {
            EmiRecipeCategory category = supplier.get();
            if (category != null) {
                categories.add(category);
            }
        }
        return categories;
    }

    private static MenuType<?> safeGetMenuType(AbstractContainerMenu menu) {
        try {
            return menu.getType();
        } catch (UnsupportedOperationException e) {
            return null;
        }
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

    private record Sorted(EmiFavorite.Craftable craftable, int index) {
    }
}
