package com.evandev.remi.feature.stackgroup.data.groups;

import com.evandev.ReliableEmi;
import com.evandev.remi.feature.stackgroup.data.StackGroup;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
//? if >=1.21 {
import net.minecraft.world.item.AnimalArmorItem;
//?} else
//import net.minecraft.world.item.HorseArmorItem;

public class AnimalArmorItemGroup extends StackGroup {
    public AnimalArmorItemGroup() {
        super(ReliableEmi.mcId("animal_armors"), null);
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack s)) return false;
        //? if >=1.21 {
        return s.getItemStack().getItem() instanceof AnimalArmorItem;
        //?} else
        //return s.getItemStack().getItem() instanceof HorseArmorItem;
    }
}
