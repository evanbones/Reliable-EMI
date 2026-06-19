package com.evandev.emixx.feature.stackgroup.data.groups;

import com.evandev.emixx.feature.stackgroup.data.StackGroup;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AnimalArmorItem;

public class AnimalArmorItemGroup extends StackGroup {
    public AnimalArmorItemGroup() {
        super(ResourceLocation.withDefaultNamespace("animal_armors"), null);
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack s)) return false;
        return s.getItemStack().getItem() instanceof AnimalArmorItem;
    }
}
