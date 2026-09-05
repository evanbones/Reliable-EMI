package com.evandev.remi.feature.stackgroup.data.groups;

import com.evandev.remi.feature.stackgroup.data.StackGroup;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;

public class CopperBlockItemGroup extends StackGroup {
    private static final net.minecraft.world.item.Item[] WAXED = {
        Items.WAXED_COPPER_BLOCK, Items.WAXED_CUT_COPPER,
        Items.WAXED_EXPOSED_COPPER, Items.WAXED_EXPOSED_CUT_COPPER,
        Items.WAXED_WEATHERED_COPPER, Items.WAXED_WEATHERED_CUT_COPPER,
        Items.WAXED_OXIDIZED_COPPER, Items.WAXED_OXIDIZED_CUT_COPPER
    };

    public CopperBlockItemGroup() {
        super(new ResourceLocation("copper_blocks"), null);
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack s)) return false;
        var item = s.getItemStack().getItem();
        String path = s.getId().getPath();
        if (!(item instanceof BlockItem bi)) return false;
        if (bi.getBlock() instanceof WeatheringCopperFullBlock) return true;
        for (var w : WAXED) if (item == w) return true;
        return path.contains("copper_bulb") || path.contains("copper_grate") || path.contains("chiseled_copper");
    }
}
