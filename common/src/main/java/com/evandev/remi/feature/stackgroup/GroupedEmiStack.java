package com.evandev.remi.feature.stackgroup;

import com.evandev.remi.feature.stackgroup.data.StackGroup;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.StackBatcher;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GroupedEmiStack<T extends EmiStack> extends EmiStack implements StackBatcher.Batchable {
    public final T realStack;
    public final EmiIngredient realIngredient;
    public final StackGroup stackGroup;

    public GroupedEmiStack(T realStack, StackGroup stackGroup) {
        this(realStack, realStack, stackGroup);
    }

    public GroupedEmiStack(T realStack, EmiIngredient realIngredient, StackGroup stackGroup) {
        this.realStack = realStack;
        this.realIngredient = realIngredient != null ? realIngredient : realStack;
        this.stackGroup = stackGroup;
        this.amount = realStack.getAmount();
        this.chance = realStack.getChance();
        this.setRemainder(realStack.getRemainder());
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        if (realIngredient != null) {
            realIngredient.render(draw, x, y, delta, flags);
        } else {
            realStack.render(draw, x, y, delta, flags);
        }
    }

    @Override
    public boolean isSideLit() {
        return realStack instanceof StackBatcher.Batchable b && b.isSideLit();
    }

    @Override
    public boolean isUnbatchable() {
        return !(realStack instanceof StackBatcher.Batchable b) || b.isUnbatchable();
    }

    @Override
    public void setUnbatchable() {
        if (realStack instanceof StackBatcher.Batchable b) b.setUnbatchable();
    }

    @Override
    public void renderForBatch(MultiBufferSource vcp, GuiGraphics draw, int x, int y, int z, float delta) {
        if (realStack instanceof StackBatcher.Batchable b) b.renderForBatch(vcp, draw, x, y, z, delta);
    }

    @Override
    public boolean isEqual(EmiStack stack) {
        if (stack instanceof GroupedEmiStack<?> gs)
            return realStack.isEqual(gs.realStack, Comparison.compareNbt());
        return realStack.isEqual(stack, Comparison.compareNbt());
    }

    @Override
    public boolean isEqual(EmiStack stack, Comparison comparison) {
        if (stack instanceof GroupedEmiStack<?> gs) return realStack.isEqual(gs.realStack, comparison);
        return realStack.isEqual(stack, comparison);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof GroupedEmiStack<?> gs)
            return realStack.isEqual(gs.realStack, Comparison.compareNbt());
        return realStack.equals(other);
    }

    @Override
    public int hashCode() {
        return realStack.hashCode();
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        if (realIngredient != null) {
            return realIngredient.getEmiStacks();
        }
        return realStack.getEmiStacks();
    }

    @Override
    public boolean isEmpty() {
        if (realIngredient != null) return realIngredient.isEmpty();
        return realStack.isEmpty();
    }

    @Override
    public EmiStack copy() {
        EmiStack copy = realStack.copy();
        copy.setAmount(this.amount);
        copy.setChance(this.chance);
        copy.setRemainder(this.getRemainder());
        return copy;
    }

    @Override
    public CompoundTag getNbt() {
        return realStack.getNbt();
    }

    @Override
    public Object getKey() {
        return realStack.getKey();
    }

    @Override
    public ResourceLocation getId() {
        return realStack.getId();
    }

    @Override
    public List<Component> getTooltipText() {
        return realStack.getTooltipText();
    }

    @Override
    public Component getName() {
        return realStack.getName();
    }

    @Override
    public <V> V getKeyOfType(Class<V> clazz) {
        return realStack.getKeyOfType(clazz);
    }

    @Override
    public ItemStack getItemStack() {
        return realStack.getItemStack();
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        if (realIngredient != null) return realIngredient.getTooltip();
        return realStack.getTooltip();
    }

    @Override
    public String toString() {
        return realStack.toString();
    }
}
