package com.evandev.emixx.feature.stackgroup;

import com.evandev.emixx.feature.stackgroup.data.StackGroup;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GroupedEmiStack<T extends EmiStack> extends EmiStack {
    public final T realStack;
    public final StackGroup stackGroup;

    public GroupedEmiStack(T realStack, StackGroup stackGroup) {
        this.realStack = realStack;
        this.stackGroup = stackGroup;
        this.amount = realStack.getAmount();
        this.chance = realStack.getChance();
        this.setRemainder(realStack.getRemainder());
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        realStack.render(draw, x, y, delta, flags);
    }

    @Override
    public boolean isEqual(EmiStack stack) {
        if (stack instanceof GroupedEmiStack<?> gs) return realStack.isEqual(gs.realStack, Comparison.compareComponents());
        return realStack.isEqual(stack, Comparison.compareComponents());
    }

    @Override
    public boolean isEqual(EmiStack stack, Comparison comparison) {
        if (stack instanceof GroupedEmiStack<?> gs) return realStack.isEqual(gs.realStack, comparison);
        return realStack.isEqual(stack, comparison);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof GroupedEmiStack<?> gs) return realStack.isEqual(gs.realStack, Comparison.compareComponents());
        return realStack.equals(other);
    }

    @Override
    public int hashCode() { return realStack.hashCode(); }

    @Override
    public List<EmiStack> getEmiStacks() { return realStack.getEmiStacks(); }

    @Override
    public boolean isEmpty() { return realStack.isEmpty(); }

    @Override
    public EmiStack copy() {
        EmiStack copy = realStack.copy();
        copy.setAmount(this.amount);
        copy.setChance(this.chance);
        copy.setRemainder(this.getRemainder());
        return copy;
    }

    @Override
    public DataComponentPatch getComponentChanges() { return realStack.getComponentChanges(); }

    @Override
    public Object getKey() { return realStack.getKey(); }

    @Override
    public ResourceLocation getId() { return realStack.getId(); }

    @Override
    public List<Component> getTooltipText() { return realStack.getTooltipText(); }

    @Override
    public Component getName() { return realStack.getName(); }

    @Override
    public <V> V getKeyOfType(Class<V> clazz) { return realStack.getKeyOfType(clazz); }

    @Override
    public ItemStack getItemStack() { return realStack.getItemStack(); }

    @Override
    public List<ClientTooltipComponent> getTooltip() { return realStack.getTooltip(); }

    @Override
    public String toString() { return realStack.toString(); }
}
