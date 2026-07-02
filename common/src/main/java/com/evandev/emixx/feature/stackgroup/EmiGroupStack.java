package com.evandev.emixx.feature.stackgroup;

import com.evandev.EmiPlusPlus;
import com.evandev.emixx.integration.emi.ScreenManager;
import com.evandev.emixx.feature.stackgroup.data.StackGroup;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiHidden;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EmiGroupStack extends EmiStack {
    public final StackGroup group;
    public boolean isExpanded = false;
    public List<GroupedEmiStack<EmiStack>> itemsNew;
    private HashSet<StackWrapper> contentLookup;

    public EmiGroupStack(StackGroup group, List<GroupedEmiStack<EmiStack>> items) {
        this.group = group;
        this.itemsNew = items;
    }

    public boolean append(GroupedEmiStack<EmiStack> stack) {
        if (contentLookup == null) {
            contentLookup = new HashSet<>();
            for (var item : itemsNew) contentLookup.add(new StackWrapper(item.realStack));
        }
        if (contentLookup.add(new StackWrapper(stack.realStack))) {
            itemsNew.add(stack);
            return true;
        }
        return false;
    }

    public List<GroupedEmiStack<EmiStack>> getItems() {
        if (EmiConfig.editMode) return itemsNew;
        List<GroupedEmiStack<EmiStack>> result = new ArrayList<>();
        for (var item : itemsNew) {
            if (!EmiHidden.isHidden(item.realStack)) result.add(item);
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return getItems().isEmpty();
    }

    @Override
    public Object getKey() {
        return group;
    }

    @Override
    public ResourceLocation getId() {
        return group.getId();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public String toString() {
        return getKey().toString();
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return List.of(
                ClientTooltipComponent.create(getName().getVisualOrderText()),
                ClientTooltipComponent.create(
                        Component.literal(String.valueOf(getItems().size()))
                                .withStyle(ChatFormatting.DARK_GRAY)
                                .append(EmiPlusPlus.text("stackgroup", "tooltip").withStyle(ChatFormatting.DARK_GRAY))
                                .getVisualOrderText()
                )
        );
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public void render(GuiGraphics raw, int x, int y, float delta, int flags) {
        List<GroupedEmiStack<EmiStack>> items = getItems();
        EmiDrawContext context = EmiDrawContext.wrap(raw);
        int es = ScreenManager.ENTRY_SIZE;
        context.push();

        if (isExpanded) {
            context.fill(x - 1, y - 1, 1, es, 0xFFFFFFFF);
            context.fill(x - 1, y - 1, es, 1, 0xFFFFFFFF);
            context.fill(x + es - 2, y - 1, 1, es, 0xFFFFFFFF);
            context.fill(x - 1, y + es - 2, es, 1, 0xFFFFFFFF);
            context.fill(x, y, es - 2, es - 2, 0x30FFFFFF);
        }

        context.matrices().pushPose();
        context.matrices().translate(x + 1.6F, y + 1.6F, 0F);
        context.matrices().scale(0.8F, 0.8F, 0.8F);

        if (items.size() == 1) {
            items.getFirst().render(raw, 0, 0, delta, flags);
        } else if (items.size() == 2) {
            context.matrices().translate(0.5F, 0F, 0F);
            items.get(1).render(raw, 1, -1, delta, flags);
            context.matrices().translate(0F, 0F, 10F);
            items.getFirst().render(raw, -2, 1, delta, flags);
        } else if (items.size() >= 3) {
            items.get(2).render(raw, 3, -2, delta, flags);
            context.matrices().translate(0F, 0F, 10F);
            items.get(1).render(raw, 0, 0, delta, flags);
            context.matrices().translate(0F, 0F, 10F);
            items.get(0).render(raw, -3, 2, delta, flags);
        }
        context.matrices().popPose();

        EmiRenderHelper.renderAmount(context, x, y, EmiPort.literal(isExpanded ? "-" : "+"));
        context.pop();
    }

    @Override
    public EmiGroupStack copy() {
        EmiGroupStack copy = new EmiGroupStack(group, new ArrayList<>(itemsNew));
        copy.isExpanded = this.isExpanded;
        return copy;
    }

    @Override
    public MutableComponent getName() {
        if (group.name != null) return (MutableComponent) group.name;

        String key = "stackgroup." + EmiPlusPlus.MOD_ID + "." + group.getId().getPath();
        if (Language.getInstance().has(key)) return Component.translatable(key);

        String tagKey = "tag.item." + group.getId().getNamespace() + "." + group.getId().getPath().replace('/', '.');
        if (Language.getInstance().has(tagKey)) return Component.translatable(tagKey);

        String[] parts = group.getId().getPath().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            if (!part.isEmpty()) sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return Component.literal(sb.toString());
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(getName(), EmiPlusPlus.text("stackgroup", "tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public int hashCode() {
        return group.hashCode();
    }

    private record StackWrapper(EmiStack stack) {
        @Override
        public boolean equals(Object o) {
            return o instanceof StackWrapper(EmiStack stack1) && stack.isEqual(stack1, Comparison.compareComponents());
        }

        @Override
        public int hashCode() {
            return stack.getId().hashCode();
        }
    }
}
