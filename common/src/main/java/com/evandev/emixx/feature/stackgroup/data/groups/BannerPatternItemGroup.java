package com.evandev.emixx.feature.stackgroup.data.groups;

import com.evandev.emixx.feature.stackgroup.data.StackGroup;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BannerPatternItem;

public class BannerPatternItemGroup extends StackGroup {
    public BannerPatternItemGroup() {
        super(ResourceLocation.withDefaultNamespace("banner_patterns"), null);
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack s)) return false;
        return s.getItemStack().getItem() instanceof BannerPatternItem;
    }
}
