package com.evandev.emixx.feature.stackgroup.data;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.regex.Pattern;

public class RegexStackGroup extends StackGroup {
    private final Pattern pattern;

    public RegexStackGroup(ResourceLocation id, Pattern pattern, Component name) {
        super(id, name);
        this.pattern = pattern;
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack emiStack)) return false;
        return pattern.matcher(emiStack.getId().toString()).matches();
    }
}
