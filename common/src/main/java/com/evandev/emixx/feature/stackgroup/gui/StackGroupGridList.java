package com.evandev.emixx.feature.stackgroup.gui;

import com.evandev.emixx.config.EmiPlusPlusConfig;
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

        private int draggedIndex = -1;

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
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) return true;
            if (button != 0 || group == null) return false;

            EmiGroupStack cachedStack = StackGroupManager.groupToGroupStacks.get(group);
            if (cachedStack == null || cachedStack.itemsNew == null) return false;

            int startX = getX() + BORDER_WIDTH + PADDING;
            int startY = getY() + BORDER_WIDTH + PADDING + ScreenManager.ENTRY_SIZE;

            for (int i = 0; i < Math.min(8, cachedStack.itemsNew.size()); i++) {
                int itemX = startX + i * ScreenManager.ENTRY_SIZE;
                if (mouseX >= itemX && mouseX < itemX + ScreenManager.ENTRY_SIZE &&
                        mouseY >= startY && mouseY < startY + ScreenManager.ENTRY_SIZE) {
                    draggedIndex = i;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (draggedIndex == -1 || button != 0) return super.mouseReleased(mouseX, mouseY, button);

            EmiGroupStack cachedStack = StackGroupManager.groupToGroupStacks.get(group);
            if (cachedStack != null && cachedStack.itemsNew != null) {
                int startX = getX() + BORDER_WIDTH + PADDING;
                int startY = getY() + BORDER_WIDTH + PADDING + ScreenManager.ENTRY_SIZE;

                if (mouseY >= startY - 10 && mouseY <= startY + ScreenManager.ENTRY_SIZE + 10) {
                    int dropIndex = (int) ((mouseX - startX) / ScreenManager.ENTRY_SIZE);
                    dropIndex = Math.max(0, Math.min(dropIndex, Math.min(8, cachedStack.itemsNew.size()) - 1));

                    if (dropIndex != draggedIndex) {
                        var item = cachedStack.itemsNew.remove(draggedIndex);
                        cachedStack.itemsNew.add(dropIndex, item);

                        List<String> newOrder = cachedStack.itemsNew.stream()
                                .map(i -> i.realStack.getId().toString())
                                .toList();
                        EmiPlusPlusConfig.stackGroupItemOrder.put(group.getId().toString(), newOrder);
                        EmiPlusPlusConfig.save();
                    }
                }
            }
            draggedIndex = -1;
            return true;
        }

        @Override
        public void renderEntry(GuiGraphics guiGraphics, int mouseX, int mouseY, int startX, int startY, float partialTick) {
            if (group == null) return;

            EmiGroupStack cachedStack = StackGroupManager.groupToGroupStacks.get(group);
            if (cachedStack == null || cachedStack.itemsNew == null) return;

            var font = Minecraft.getInstance().font;
            int itemY = startY + ScreenManager.ENTRY_SIZE;

            int currentDropIndex = draggedIndex;
            if (draggedIndex != -1 && mouseY >= itemY - 10 && mouseY <= itemY + ScreenManager.ENTRY_SIZE + 10) {
                currentDropIndex = (mouseX - startX) / ScreenManager.ENTRY_SIZE;
                currentDropIndex = Math.max(0, Math.min(currentDropIndex, Math.min(8, cachedStack.itemsNew.size()) - 1));
            }

            var displayList = new ArrayList<>(cachedStack.itemsNew);
            if (draggedIndex != -1 && currentDropIndex != draggedIndex) {
                var item = displayList.remove(draggedIndex);
                displayList.add(currentDropIndex, item);
            }

            for (int i = 0; i < Math.min(8, displayList.size()); i++) {
                if (draggedIndex != -1 && i == currentDropIndex) continue;

                int itemX = startX + i * ScreenManager.ENTRY_SIZE;
                var item = displayList.get(i);

                boolean hovered = draggedIndex == -1 &&
                        mouseX >= itemX && mouseX < itemX + ScreenManager.ENTRY_SIZE &&
                        mouseY >= itemY && mouseY < itemY + ScreenManager.ENTRY_SIZE;

                guiGraphics.pose().pushPose();
                if (hovered) {
                    guiGraphics.pose().translate(itemX + 8, itemY + 8, 100);
                    guiGraphics.pose().scale(1.2F, 1.2F, 1.2F);
                    guiGraphics.pose().translate(-(itemX + 8), -(itemY + 8), -100);
                }

                guiGraphics.renderItem(item.getItemStack(), itemX, itemY);
                guiGraphics.renderItemDecorations(font, item.getItemStack(), itemX, itemY, "");
                guiGraphics.pose().popPose();
            }

            if (draggedIndex != -1 && draggedIndex < cachedStack.itemsNew.size()) {
                var draggedItem = cachedStack.itemsNew.get(draggedIndex);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.renderItem(draggedItem.getItemStack(), mouseX - 8, mouseY - 8);
                guiGraphics.renderItemDecorations(font, draggedItem.getItemStack(), mouseX - 8, mouseY - 8, "");
                guiGraphics.pose().popPose();
            }
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
            if (cachedStack != null) return cachedStack.getName();
            return group.name != null ? group.name : Component.empty();
        }
    }
}