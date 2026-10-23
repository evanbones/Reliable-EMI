package com.evandev.remi.feature.creativemodetab.gui;

import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.remi.gui.GridList;
import com.evandev.remi.gui.ListEntry;
import com.evandev.remi.gui.components.Switch;
import com.evandev.remi.integration.emi.ScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CreativeModeTabGridList extends GridList<ResourceLocation> {
    private final Set<ResourceLocation> disabledCreativeModeTabs;

    public CreativeModeTabGridList(CreativeModeTabConfigScreen screen, Set<ResourceLocation> disabledCreativeModeTabs) {
        super(screen);
        this.disabledCreativeModeTabs = disabledCreativeModeTabs;
    }

    @Override
    public Collection<ResourceLocation> getContents() {
        List<ResourceLocation> result = new ArrayList<>();
        for (ResourceLocation key : BuiltInRegistries.CREATIVE_MODE_TAB.keySet()) {
            CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(key);
            if (tab == null) continue;
            boolean notEmpty = !tab.getDisplayItems().isEmpty() || Minecraft.getInstance().level == null;
            if (notEmpty && !CreativeModeTabManager.HIDDEN_CREATIVE_MODE_TABS.contains(tab)) {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public ListEntry getEntryForContent(ResourceLocation content, TripleEntry<ResourceLocation> triple) {
        return new StackGroupEntry(content, triple, disabledCreativeModeTabs,
                content != null ? BuiltInRegistries.CREATIVE_MODE_TAB.get(content) : null);
    }

    public static class StackGroupEntry extends ListEntry {
        private final ResourceLocation id;
        private final CreativeModeTab tab;
        private final Switch switchWidget;
        private final List<AbstractWidget> childWidgets;

        public StackGroupEntry(ResourceLocation id, TripleEntry<ResourceLocation> triple,
                               Set<ResourceLocation> disabledTabs, CreativeModeTab tab) {
            super(triple);
            this.id = id;
            this.tab = tab;
            boolean checked = id != null && !disabledTabs.contains(id);
            this.switchWidget = new Switch.Builder(Component.empty())
                    .setChecked(checked)
                    .onCheckedChangeListener((sw, isChecked) -> {
                        if (id != null) {
                            if (isChecked) disabledTabs.remove(id);
                            else disabledTabs.add(id);
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
            return tab != null && id != null;
        }

        @Override
        public Component getEntryTitle() {
            return tab != null ? tab.getDisplayName() : null;
        }

        @Override
        public void renderEntry(GuiGraphics guiGraphics, int mouseX, int mouseY, int startX, int startY, float partialTick) {
            if (tab == null) return;
            var font = Minecraft.getInstance().font;
            int itemX = startX;
            int itemY = startY + ScreenManager.ENTRY_SIZE;
            int count = 0;
            for (var item : tab.getDisplayItems()) {
                if (item.isEmpty()) continue;
                if (count >= 8) break;
                guiGraphics.renderItem(item, itemX, itemY);
                guiGraphics.renderItemDecorations(font, item, itemX, itemY, "");
                itemX += ScreenManager.ENTRY_SIZE;
                count++;
            }
        }
    }
}
