package com.evandev.emixx.feature.stackgroup.data.groups;

import com.evandev.emixx.feature.stackgroup.data.StackGroup;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.PressurePlateBlock;

public class PressurePlateItemGroup extends StackGroup {
    public PressurePlateItemGroup() {
        super(ResourceLocation.withDefaultNamespace("pressure_plates"), null);
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack s)) return false;
        var item = s.getItemStack().getItem();
        return item instanceof BlockItem bi && bi.getBlock() instanceof PressurePlateBlock;
    }
}
