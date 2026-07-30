package com.evandev.remi.feature.stackgroup.data.groups;

import com.evandev.remi.feature.stackgroup.data.StackGroup;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.InfestedBlock;

public class InfestedBlockItemGroup extends StackGroup {
    public InfestedBlockItemGroup() {
        super(ResourceLocation.withDefaultNamespace("infested_blocks"), null);
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack s)) return false;
        var item = s.getItemStack().getItem();
        return item instanceof BlockItem bi && bi.getBlock() instanceof InfestedBlock;
    }
}
