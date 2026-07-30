package com.evandev.remi.feature.stackgroup.gui;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.stackgroup.EmiGroupStack;
import com.evandev.remi.feature.stackgroup.StackGroupManager;
import com.evandev.remi.feature.stackgroup.data.StackGroup;
import com.evandev.remi.gui.GridList;
import com.evandev.remi.gui.ListEntry;
import com.evandev.remi.gui.components.Switch;
import com.evandev.remi.integration.emi.ScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StackGroupGridList extends GridList<StackGroup> {
    private final Set<ResourceLocation> disabledStackGroups;
    private String searchQuery = "";

    public StackGroupGridList(StackGroupConfigScreen screen, Set<ResourceLocation> disabledStackGroups) {
        super(screen);
        this.disabledStackGroups = disabledStackGroups;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
    }

    @Override
    public Collection<StackGroup> getContents() {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return StackGroupManager.stackGroups;
        }

        List<StackGroup> filtered = new ArrayList<>();
        for (StackGroup group : StackGroupManager.stackGroups) {
            String name = group.name != null ? group.name.getString().toLowerCase(Locale.ROOT) : "";
            String id = group.getId().toString().toLowerCase(Locale.ROOT);
            if (name.contains(searchQuery) || id.contains(searchQuery)) {
                filtered.add(group);
            }
        }
        return filtered;
    }

    @Override
    public ListEntry getEntryForContent(StackGroup content, TripleEntry<StackGroup> triple) {
        return new StackGroupEntry(content, triple, disabledStackGroups);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (StackGroupEntry.activeExpandedEntry != null) {
            if (StackGroupEntry.activeExpandedEntry.handleDropdownClick(mouseX, mouseY)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (StackGroupEntry.activeExpandedEntry != null && StackGroupEntry.activeExpandedEntry.draggedIndex != -1) {
            StackGroupEntry.activeExpandedEntry.handleDropdownRelease(mouseX, mouseY, button);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public static class StackGroupEntry extends ListEntry {
        public static StackGroupEntry activeExpandedEntry = null;
        private final StackGroup group;
        private final Switch switchWidget;
        private final List<AbstractWidget> childWidgets;
        public boolean isTitleHovered = false;
        public int draggedIndex = -1;
        public boolean isExpanded = false;

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

            boolean canExpand = cachedStack.itemsNew.size() > 8;
            if (canExpand) {
                var font = Minecraft.getInstance().font;
                int titleX = getX() + BORDER_WIDTH + PADDING;
                int titleY = getY() + BORDER_WIDTH + PADDING + 2;

                if (mouseX >= titleX && mouseX <= titleX + font.width(getEntryTitle()) &&
                        mouseY >= titleY && mouseY <= titleY + font.lineHeight) {

                    isExpanded = !isExpanded;
                    if (isExpanded) {
                        if (activeExpandedEntry != null && activeExpandedEntry != this) {
                            activeExpandedEntry.isExpanded = false;
                        }
                        activeExpandedEntry = this;
                    } else if (activeExpandedEntry == this) {
                        activeExpandedEntry = null;
                    }
                    return true;
                }
            }

            if (!isExpanded) {
                int startX = getX() + BORDER_WIDTH + PADDING;
                int itemY = getY() + BORDER_WIDTH + PADDING + ScreenManager.ENTRY_SIZE;
                for (int i = 0; i < Math.min(8, cachedStack.itemsNew.size()); i++) {
                    int itemX = startX + i * ScreenManager.ENTRY_SIZE;
                    if (mouseX >= itemX && mouseX < itemX + ScreenManager.ENTRY_SIZE &&
                            mouseY >= itemY && mouseY < itemY + ScreenManager.ENTRY_SIZE) {
                        draggedIndex = i;
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean handleDropdownClick(double mouseX, double mouseY) {
            if (!isExpanded || group == null) return false;
            EmiGroupStack cachedStack = StackGroupManager.groupToGroupStacks.get(group);
            if (cachedStack == null || cachedStack.itemsNew == null) return false;

            int startX = getX() + BORDER_WIDTH + PADDING;
            int itemY = getY() + BORDER_WIDTH + PADDING + ScreenManager.ENTRY_SIZE;
            int totalItems = cachedStack.itemsNew.size();
            int rows = (int) Math.ceil(totalItems / 8.0);
            int dropDownHeight = rows * ScreenManager.ENTRY_SIZE;

            if (mouseX >= startX && mouseX < startX + 8 * ScreenManager.ENTRY_SIZE &&
                    mouseY >= itemY && mouseY < itemY + dropDownHeight) {

                int col = (int) ((mouseX - startX) / ScreenManager.ENTRY_SIZE);
                int row = (int) ((mouseY - itemY) / ScreenManager.ENTRY_SIZE);
                int index = row * 8 + col;

                if (index >= 0 && index < totalItems) {
                    draggedIndex = index;
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (draggedIndex == -1 || button != 0) return super.mouseReleased(mouseX, mouseY, button);

            if (!isExpanded) {
                EmiGroupStack cachedStack = StackGroupManager.groupToGroupStacks.get(group);
                if (cachedStack != null && cachedStack.itemsNew != null) {
                    int startX = getX() + BORDER_WIDTH + PADDING;
                    int itemY = getY() + BORDER_WIDTH + PADDING + ScreenManager.ENTRY_SIZE;

                    if (mouseY >= itemY - 10 && mouseY <= itemY + ScreenManager.ENTRY_SIZE + 10) {
                        int col = (int) Math.floor((mouseX - startX) / (double) ScreenManager.ENTRY_SIZE);
                        int dropIndex = Math.max(0, Math.min(col, Math.min(8, cachedStack.itemsNew.size()) - 1));

                        processItemReorder(cachedStack, dropIndex);
                    }
                }
            }
            draggedIndex = -1;
            return true;
        }

        public void handleDropdownRelease(double mouseX, double mouseY, int button) {
            if (draggedIndex == -1 || button != 0 || group == null) return;

            EmiGroupStack cachedStack = StackGroupManager.groupToGroupStacks.get(group);
            if (cachedStack != null && cachedStack.itemsNew != null) {
                int startX = getX() + BORDER_WIDTH + PADDING;
                int itemY = getY() + BORDER_WIDTH + PADDING + ScreenManager.ENTRY_SIZE;
                int totalItems = cachedStack.itemsNew.size();
                int rows = (int) Math.ceil(totalItems / 8.0);

                if (mouseY >= itemY - 10 && mouseY <= itemY + rows * ScreenManager.ENTRY_SIZE + 10) {
                    int col = (int) Math.floor((mouseX - startX) / (double) ScreenManager.ENTRY_SIZE);
                    int row = (int) Math.floor((mouseY - itemY) / (double) ScreenManager.ENTRY_SIZE);

                    if (col < 0) col = 0;
                    if (col > 7) col = 7;
                    if (row < 0) row = 0;
                    if (row >= rows) row = rows - 1;

                    int dropIndex = row * 8 + col;
                    dropIndex = Math.max(0, Math.min(dropIndex, totalItems - 1));

                    processItemReorder(cachedStack, dropIndex);
                }
            }
            draggedIndex = -1;
        }

        private void processItemReorder(EmiGroupStack cachedStack, int dropIndex) {
            if (dropIndex != draggedIndex) {
                var item = cachedStack.itemsNew.remove(draggedIndex);
                cachedStack.itemsNew.add(dropIndex, item);
                cachedStack.invalidateCaches();

                List<String> newOrder = cachedStack.itemsNew.stream()
                        .map(i -> i.realStack.getId().toString())
                        .toList();
                ReliableEmiConfig.stackGroupItemOrder.put(group.getId().toString(), newOrder);
                ReliableEmiConfig.save();
            }
        }

        @Override
        public void renderEntry(GuiGraphics guiGraphics, int mouseX, int mouseY, int startX, int startY, float partialTick) {
            if (group == null) return;

            EmiGroupStack cachedStack = StackGroupManager.groupToGroupStacks.get(group);
            if (cachedStack == null || cachedStack.itemsNew == null) return;

            var font = Minecraft.getInstance().font;
            int totalItems = cachedStack.itemsNew.size();
            int itemY = startY + ScreenManager.ENTRY_SIZE;
            int maxItems = isExpanded ? totalItems : Math.min(8, totalItems);
            int rows = isExpanded ? (int) Math.ceil(totalItems / 8.0) : 1;

            int currentDropIndex = draggedIndex;
            if (draggedIndex != -1 && mouseY >= itemY - 10 && mouseY <= itemY + rows * ScreenManager.ENTRY_SIZE + 10) {
                int col = (int) Math.floor((mouseX - startX) / (double) ScreenManager.ENTRY_SIZE);
                int row = isExpanded ? (int) Math.floor((mouseY - itemY) / (double) ScreenManager.ENTRY_SIZE) : 0;

                if (col < 0) col = 0;
                if (col > 7) col = 7;
                if (row < 0) row = 0;
                if (row >= rows) row = rows - 1;

                currentDropIndex = row * 8 + col;
                currentDropIndex = Math.max(0, Math.min(currentDropIndex, maxItems - 1));
            }

            var displayList = new ArrayList<>(cachedStack.itemsNew);
            if (draggedIndex != -1 && currentDropIndex != draggedIndex && currentDropIndex < displayList.size() && draggedIndex < displayList.size()) {
                var item = displayList.remove(draggedIndex);
                displayList.add(currentDropIndex, item);
            }

            guiGraphics.pose().pushPose();

            if (isExpanded) {
                guiGraphics.pose().translate(0, 0, 300);
                int dropDownHeight = rows * ScreenManager.ENTRY_SIZE;

                guiGraphics.fill(startX - 2, itemY - 2, startX + 8 * ScreenManager.ENTRY_SIZE + 2, itemY + dropDownHeight + 2, 0xFF1A1A1A);
                guiGraphics.fill(startX - 3, itemY - 3, startX + 8 * ScreenManager.ENTRY_SIZE + 3, itemY - 2, 0xFF555555);
                guiGraphics.fill(startX - 3, itemY + dropDownHeight + 2, startX + 8 * ScreenManager.ENTRY_SIZE + 3, itemY + dropDownHeight + 3, 0xFF555555);
                guiGraphics.fill(startX - 3, itemY - 2, startX - 2, itemY + dropDownHeight + 2, 0xFF555555);
                guiGraphics.fill(startX + 8 * ScreenManager.ENTRY_SIZE + 2, itemY - 2, startX + 8 * ScreenManager.ENTRY_SIZE + 3, itemY + dropDownHeight + 2, 0xFF555555);
            }

            for (int i = 0; i < maxItems; i++) {
                if (draggedIndex != -1 && i == currentDropIndex) continue;

                int col = i % 8;
                int row = i / 8;
                int itemX = startX + col * ScreenManager.ENTRY_SIZE;
                int currentItemY = itemY + row * ScreenManager.ENTRY_SIZE;
                var item = displayList.get(i);

                boolean hovered = draggedIndex == -1 &&
                        mouseX >= itemX && mouseX < itemX + ScreenManager.ENTRY_SIZE &&
                        mouseY >= currentItemY && mouseY < currentItemY + ScreenManager.ENTRY_SIZE;

                guiGraphics.pose().pushPose();

                if (hovered) {
                    guiGraphics.pose().translate(itemX + 8, currentItemY + 8, 100);
                    guiGraphics.pose().scale(1.2F, 1.2F, 1.2F);
                    guiGraphics.pose().translate(-(itemX + 8), -(currentItemY + 8), -100);
                }

                guiGraphics.renderItem(item.getItemStack(), itemX, currentItemY);
                guiGraphics.renderItemDecorations(font, item.getItemStack(), itemX, currentItemY, "");
                guiGraphics.pose().popPose();
            }
            guiGraphics.pose().popPose();

            if (draggedIndex != -1 && draggedIndex < cachedStack.itemsNew.size()) {
                var draggedItem = cachedStack.itemsNew.get(draggedIndex);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 400);
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
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            var font = Minecraft.getInstance().font;
            int titleX = getX() + BORDER_WIDTH + PADDING;
            int titleY = getY() + BORDER_WIDTH + PADDING + 2;

            this.isTitleHovered = mouseX >= titleX && mouseX <= titleX + font.width(getEntryTitle()) &&
                    mouseY >= titleY && mouseY <= titleY + font.lineHeight;

            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public Component getEntryTitle() {
            if (group == null) return Component.empty();

            Component baseName;
            EmiGroupStack cachedStack = StackGroupManager.groupToGroupStacks.get(group);
            if (cachedStack != null) {
                baseName = cachedStack.getName();
            } else {
                baseName = group.name != null ? group.name : Component.empty();
            }

            if (cachedStack != null && cachedStack.itemsNew != null && cachedStack.itemsNew.size() > 8) {
                int color = isTitleHovered ? 0xFFFFFF : (isExpanded ? 0xFFAA00 : 0x00AAFF);

                return Component.literal(isExpanded ? "[-] " : "[+] ")
                        .append(baseName)
                        .withStyle(Style.EMPTY.withColor(color));
            }

            return baseName;
        }
    }
}