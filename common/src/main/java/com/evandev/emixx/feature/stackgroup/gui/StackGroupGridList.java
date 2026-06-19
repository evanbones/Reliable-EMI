package com.evandev.emixx.feature.stackgroup.gui;

import com.evandev.emixx.feature.stackgroup.EmiGroupStack;
import com.evandev.emixx.feature.stackgroup.StackGroupManager;
import com.evandev.emixx.feature.stackgroup.data.StackGroup;
import com.evandev.emixx.gui.GridList;
import com.evandev.emixx.gui.ListEntry;
import com.evandev.emixx.gui.components.Switch;
import com.evandev.emixx.integration.emi.ScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class StackGroupGridList extends GridList<StackGroup> {
    private final Set<ResourceLocation> disabledStackGroups;

    public StackGroupGridList(StackGroupConfigScreen screen, Set<ResourceLocation> disabledStackGroups) {
        super(screen);
        this.disabledStackGroups = disabledStackGroups;
    }

    @Override
    public Collection<StackGroup> getContents() {
        return StackGroupManager.stackGroups;
    }

    @Override
    public ListEntry getEntryForContent(StackGroup content, TripleEntry<StackGroup> triple) {
        return new StackGroupEntry(content, triple, disabledStackGroups);
    }

    public static class StackGroupEntry extends ListEntry {
        private final StackGroup group;
        private final Switch switchWidget;
        private final List<AbstractWidget> childWidgets;

        public StackGroupEntry(StackGroup group, TripleEntry<StackGroup> triple, Set<ResourceLocation> disabledGroups) {
            super(triple);
            this.group = group;
            boolean checked = group != null && !disabledGroups.contains(group.getId());

            this.switchWidget = new Switch.Builder(Component.empty())
                    .setChecked(checked)
                    .onCheckedChangeListener((sw, isChecked) -> {
                        if (group != null) {
                            if (isChecked) disabledGroups.remove(group.getId());
                            else disabledGroups.add(group.getId());
                        }
                    })
                    .build();

            this.childWidgets = new ArrayList<>(List.of(switchWidget));
        }

        @Override
        protected Switch getSwitch() {
            return switchWidget;
        }

        @Override
        protected List<AbstractWidget> getChildren() {
            return childWidgets;
        }

        @Override
        public boolean shouldRenderSwitch() {
            return group != null;
        }

        @Override
        public Component getEntryTitle() {
            if (group == null) return Component.empty();

            EmiGroupStack cachedStack = StackGroupManager.groupToGroupStacks.get(group);
            if (cachedStack != null) {
                return cachedStack.getName();
            }

            return group.name != null ? group.name : Component.empty();
        }

        @Override
        public void renderEntry(GuiGraphics guiGraphics, int mouseX, int mouseY, int startX, int startY, float partialTick) {
            if (group == null) return;

            EmiGroupStack cachedStack = StackGroupManager.groupToGroupStacks.get(group);
            if (cachedStack == null || cachedStack.itemsNew == null) return;

            var font = Minecraft.getInstance().font;
            int itemX = startX;
            int itemY = startY + ScreenManager.ENTRY_SIZE;
            int count = 0;

            for (var item : cachedStack.itemsNew) {
                if (count >= 8) break;
                guiGraphics.renderItem(item.getItemStack(), itemX, itemY);
                guiGraphics.renderItemDecorations(font, item.getItemStack(), itemX, itemY, "");
                itemX += ScreenManager.ENTRY_SIZE;
                count++;
            }
        }
    }
}