package com.evandev.emixx.feature.stackgroup.data;

import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public abstract class StackGroup {
    private final ResourceLocation id;
    public final Component name;
    public boolean isEnabled = true;
    public int priority = 0;

    protected StackGroup(ResourceLocation id, Component name) {
        this.id = id;
        this.name = name;
    }

    public ResourceLocation getId() { return id; }

    public abstract boolean match(EmiIngredient stack);

    public Set<ResourceLocation> getOptimizedIds() {
        return null;
    }
}