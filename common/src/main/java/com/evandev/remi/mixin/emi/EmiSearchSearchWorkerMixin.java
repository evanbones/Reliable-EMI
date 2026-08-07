package com.evandev.remi.mixin.emi;

import com.evandev.remi.feature.stackgroup.StackGroupManager;
import com.evandev.remi.integration.emi.StackManager;
import com.evandev.remi.util.SearchWorkerBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "dev.emi.emi.search.EmiSearch$SearchWorker", remap = false)
public class EmiSearchSearchWorkerMixin implements SearchWorkerBridge {

    @Final
    @Shadow
    private String query;

    @Override
    public String remi$getQuery() {
        return this.query;
    }

    @WrapOperation(method = "run", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/search/EmiSearch;apply(Ldev/emi/emi/search/EmiSearch$SearchWorker;Ljava/util/List;)V"))
    private void run(@Coerce Object worker, List<? extends EmiIngredient> stacks, Operation<Void> original) {
        synchronized (EmiSearch.class) {
            EmiScreenManager.SidebarPanel searchPanel = EmiScreenManager.getSearchPanel();
            boolean isIndex = searchPanel != null && searchPanel.getType() == SidebarType.INDEX;
            if (isIndex) {
                List<EmiStack> combinedStacks = new ArrayList<>();
                for (EmiIngredient stack : stacks) {
                    if (stack instanceof EmiStack emiStack) combinedStacks.add(emiStack);
                }

                String query = ((SearchWorkerBridge) worker).remi$getQuery();
                if (query != null && !query.isEmpty()) {
                    String cleanQuery = query.startsWith("%") ? query.substring(1) : query;
                    if (!cleanQuery.isEmpty()) {
                        StackGroupManager.appendStacksForMatchingGroups(cleanQuery, combinedStacks);
                    }
                }

                StackManager.buildStacks(combinedStacks);
            } else {
                original.call(worker, stacks);
            }
        }
    }
}