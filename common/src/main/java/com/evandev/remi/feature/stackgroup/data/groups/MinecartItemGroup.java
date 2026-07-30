package com.evandev.remi.feature.stackgroup.data.groups;

import com.evandev.remi.feature.stackgroup.data.StackGroup;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.MinecartItem;

public class MinecartItemGroup extends StackGroup {
    public MinecartItemGroup() {
        super(ResourceLocation.withDefaultNamespace("minecarts"), null);
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack s)) return false;
        return s.getItemStack().getItem() instanceof MinecartItem;
    }
}
