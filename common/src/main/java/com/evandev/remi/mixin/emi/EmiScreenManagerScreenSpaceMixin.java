package com.evandev.remi.mixin.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.remi.feature.stackgroup.EmiGroupStack;
import com.evandev.remi.feature.stackgroup.StackGroupManager;
import com.evandev.remi.feature.workstation.WorkstationSidebarManager;
import com.evandev.remi.integration.emi.Layout;
import com.evandev.remi.integration.emi.StackManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiHidden;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.StackBatcher;
import dev.emi.emi.search.EmiSearch;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.evandev.remi.integration.emi.ScreenManager.ENTRY_SIZE;

@Mixin(value = EmiScreenManager.ScreenSpace.class, remap = false)
public abstract class EmiScreenManagerScreenSpaceMixin {

    @Shadow
    @Final
    public int pageSize;

    @Shadow
    @Final
    public StackBatcher batcher;

    @Shadow
    @Final
    public int th;

    @Shadow
    @Final
    public int tw;

    @Unique
    private String remi$lastSearchValue;

    @Unique
    private EmiSearch.CompiledQuery remi$compiledQuery;

    @Shadow
    public abstract List<? extends EmiIngredient> getStacks();

    @Shadow
    public abstract int getRawOffsetFromMouse(int mouseX, int mouseY);

    @Shadow
    public abstract int getRawX(int off);

    @Shadow
    public abstract int getRawY(int off);

    @Shadow
    public abstract int getWidth(int y);

    @Shadow
    public abstract int getX(int x, int y);

    @Shadow
    public abstract int getY(int x, int y);

    @Shadow
    public abstract SidebarType getType();

    @Inject(method = "getStacks", at = @At("HEAD"), cancellable = true)
    private void remi$getCorrectStacks(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        SidebarType type = getType();
        if (type == SidebarType.CHESS || type == SidebarType.NONE || type == SidebarType.EMPTY) {
            return;
        }

        List<? extends EmiIngredient> stacks;
        if (type == SidebarType.INDEX) {
            stacks = StackManager.displayedStacks;
            if (!ReliableEmiConfig.isStackGroupsEnabled(SidebarType.INDEX)) {
                List<EmiIngredient> ungrouped = new ArrayList<>();
                for (EmiIngredient ing : stacks) {
                    if (ing instanceof EmiGroupStack gs) {
                        for (var item : gs.itemsNew) ungrouped.add(item.realStack);
                    } else {
                        ungrouped.add(ing);
                    }
                }
                stacks = ungrouped;
            }
        } else {
            if (WorkstationSidebarManager.WORKSTATION != null && type == WorkstationSidebarManager.WORKSTATION) {
                stacks = WorkstationSidebarManager.workstationStacks;
            } else {
                stacks = EmiSidebars.getStacks(type);
            }
            if (stacks == null) stacks = List.of();

            if (ReliableEmiConfig.isCreativeTabsEnabled(type)) {
                List<EmiIngredient> tabFiltered = new ArrayList<>();
                for (EmiIngredient ing : stacks) {
                    if (CreativeModeTabManager.isIngredientInCurrentTab(ing)) {
                        tabFiltered.add(ing);
                    }
                }
                stacks = tabFiltered;
            }

            if (EmiScreenManager.search != null) {
                String searchValue = EmiScreenManager.search.getValue();
                if (!Objects.equals(searchValue, remi$lastSearchValue)) {
                    remi$lastSearchValue = searchValue;
                    remi$compiledQuery = searchValue.isEmpty() ? null : new EmiSearch.CompiledQuery(searchValue);
                    if (batcher != null) {
                        batcher.repopulate();
                    }
                }
                if (remi$compiledQuery != null && !remi$compiledQuery.isEmpty()) {
                    List<EmiIngredient> searchFiltered = new ArrayList<>();
                    for (EmiIngredient ing : stacks) {
                        List<EmiStack> emiStacks = ing.getEmiStacks();
                        if (!emiStacks.isEmpty() && remi$compiledQuery.test(emiStacks.getFirst())) {
                            searchFiltered.add(ing);
                        }
                    }
                    stacks = searchFiltered;
                }
            }

            if (ReliableEmiConfig.isStackGroupsEnabled(type)) {
                stacks = StackGroupManager.buildGroupedIngredients(stacks, type);
            }
        }
        cir.setReturnValue(stacks);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void createGrid(int tx, int ty, int tw, int th, boolean rtl, List<Bounds> exclusion,
                            Supplier<SidebarType> typeSupplier, boolean search, CallbackInfo ci) {
        if (ReliableEmiConfig.isStackGroupsEnabled(getType()))
            StackManager.stackGrid = new EmiStack[th + 9][tw + 9];
    }

    /**
     * @author evanbones
     * @reason Capture grid coordinates for stack group layout rendering.
     */
    @Overwrite
    public void render(EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex) {
        if (ReliableEmiConfig.isStackGroupsEnabled(getType())) {
            Layout.checkGridSize(tw, th);
        }

        if (pageSize > 0) {
            RenderSystem.enableDepthTest();
            EmiPort.setPositionTexShader();
            context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            batcher.begin(0, 0, 0);
            int i = startIndex;
            List<? extends EmiIngredient> stacks = getStacks();
            int hovered = getRawOffsetFromMouse(mouseX, mouseY);
            if (hovered != -1 && EmiConfig.showHoverOverlay && startIndex + hovered < stacks.size()) {
                EmiRenderHelper.drawSlotHightlight(context, getRawX(hovered), getRawY(hovered), ENTRY_SIZE, ENTRY_SIZE, 0);
            }
            context.push();

            if (ReliableEmiConfig.isStackGroupsEnabled(getType())) {
                for (EmiStack[] row : StackManager.stackGrid) {
                    if (row != null) Arrays.fill(row, null);
                }
            }

            outer:
            for (int yo = 0; yo < th; yo++) {
                for (int xo = 0; xo < getWidth(yo); xo++) {
                    if (i >= stacks.size()) break outer;
                    int cx = getX(xo, yo);
                    int cy = getY(xo, yo);
                    EmiIngredient stack = stacks.get(i++);

                    if (ReliableEmiConfig.isStackGroupsEnabled(getType())) {
                        EmiStack gridStack = stack instanceof EmiStack es ? es : (stack != null && !stack.getEmiStacks().isEmpty() ? stack.getEmiStacks().getFirst() : null);
                        if (gridStack != null) {
                            StackManager.stackGrid[yo][xo] = gridStack;
                        }
                    }

                    batcher.render(stack, context.raw(), cx + 1, cy + 1, delta);
                    if (getType() == SidebarType.INDEX) {
                        if (EmiConfig.editMode && EmiHidden.isHidden(stack)) {
                            RenderSystem.enableDepthTest();
                            context.fill(cx, cy, ENTRY_SIZE, ENTRY_SIZE, 0x33ff0000);
                        } else if (EmiConfig.highlightDefaulted && BoM.getRecipe(stack) != null) {
                            RenderSystem.enableDepthTest();
                            context.fill(cx, cy, ENTRY_SIZE, ENTRY_SIZE, 0x3300ff00);
                        }
                    }
                }
            }
            batcher.draw();
            context.pop();
            if (ReliableEmiConfig.isStackGroupsEnabled(getType()))
                Layout.buildLayoutTiles(EmiScreenManager.ScreenSpace.class.cast(this), context);
        }
    }
}
