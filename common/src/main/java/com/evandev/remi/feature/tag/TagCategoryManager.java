package com.evandev.remi.feature.tag;

import com.evandev.ReliableEmi;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;

public class TagCategoryManager {

    public static final EmiRecipeCategory ITEM_TAGS = new EmiRecipeCategory(
            ReliableEmi.res("item_tags"),
            EmiStack.of(Items.NAME_TAG)
    );

    public static final EmiRecipeCategory BLOCK_TAGS = new EmiRecipeCategory(
            ReliableEmi.res("block_tags"),
            EmiStack.of(Items.NAME_TAG)
    );

    public static final EmiRecipeCategory FLUID_TAGS = new EmiRecipeCategory(
            ReliableEmi.res("fluid_tags"),
            EmiStack.of(Items.NAME_TAG)
    );

    public static final EmiRecipeCategory ENTITY_TYPE_TAGS = new EmiRecipeCategory(
            ReliableEmi.res("entity_type_tags"),
            EmiStack.of(Items.NAME_TAG)
    );

    public static EmiRecipeCategory getCategory(TagKey<?> key) {
        if (key == null) return VanillaPlugin.TAG;
        var regKey = key.registry();
        if (regKey.equals(BuiltInRegistries.ITEM.key())) {
            return ITEM_TAGS;
        } else if (regKey.equals(BuiltInRegistries.BLOCK.key())) {
            return BLOCK_TAGS;
        } else if (regKey.equals(BuiltInRegistries.FLUID.key())) {
            return FLUID_TAGS;
        } else if (regKey.equals(BuiltInRegistries.ENTITY_TYPE.key())) {
            return ENTITY_TYPE_TAGS;
        }
        return VanillaPlugin.TAG;
    }
}
