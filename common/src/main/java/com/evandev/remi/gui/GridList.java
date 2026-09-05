package com.evandev.remi.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class GridList<Contents> extends ContainerObjectSelectionList<GridList.TripleEntry<Contents>> {

    protected final Screen screen;

    protected GridList(Screen screen) {
        super(Minecraft.getInstance(), screen.width, screen.height, 0, screen.height, TripleEntry.HEIGHT);
        this.screen = screen;
        centerListVertically = false;
        setRenderHeader(true, 16);
    }

    @Override
    public int getRowWidth() {
        return TripleEntry.WIDTH;
    }

    @Override
    public int getScrollbarPosition() {
        return this.width - 6;
    }

    public abstract Collection<Contents> getContents();

    public abstract ListEntry getEntryForContent(Contents content, TripleEntry<Contents> triple);

    public void add() {
        List<Contents> contents = new ArrayList<>(getContents());
        for (int i = 0; i < contents.size(); i += 3) {
            List<Contents> chunk = contents.subList(i, Math.min(i + 3, contents.size()));
            addEntry(new TripleEntry<>(this, chunk));
        }
    }

    public void refreshList() {
        this.clearEntries();
        this.add();
        this.setScrollAmount(0);
    }

    public static class TripleEntry<Contents> extends Entry<TripleEntry<Contents>> {
        static final int GUTTER = 6;
        static final int WIDTH = ListEntry.WIDTH * 3 + GUTTER * 2;
        static final int HEIGHT = ListEntry.HEIGHT + GUTTER * 2;

        private final GridList<Contents> listWidget;
        private final List<ListEntry> entries = new ArrayList<>();

        public TripleEntry(GridList<Contents> listWidget, List<Contents> contentsList) {
            this.listWidget = listWidget;
            for (int i = 0; i < 3; i++) {
                if (i < contentsList.size()) {
                    entries.add(listWidget.getEntryForContent(contentsList.get(i), this));
                }
            }
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean isHovered, float partialTick) {
            int xOffset = 0;
            int startX = (listWidget.screen.width - WIDTH) / 2;
            for (ListEntry entry : entries) {
                entry.setPosition(startX + xOffset, top);
                entry.render(guiGraphics, mouseX, mouseY, partialTick);
                xOffset += ListEntry.WIDTH + GUTTER * 2;
            }
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return entries;
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return entries;
        }
    }
}
