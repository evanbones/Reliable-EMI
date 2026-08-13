package com.evandev.remi.gui.components;

import com.evandev.ReliableEmi;
import com.evandev.remi.util.SidebarPanelWithScrollOffset;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.config.SidebarTheme;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ScrollbarWidget extends AbstractWidget {
    private final EmiScreenManager.SidebarPanel panel;

    private static final ResourceLocation TRACK_SPRITES = ReliableEmi.res("widget/scrollbar_track");
    private static final ResourceLocation THUMB_SPRITES = ReliableEmi.res("widget/scrollbar_thumb");
    private static final ResourceLocation VANILLA_TRACK_SPRITES = ReliableEmi.res("widget/scrollbar_track_vanilla");
    private static final ResourceLocation VANILLA_THUMB_SPRITES = ReliableEmi.res("widget/scrollbar_thumb_vanilla");
    private static final ResourceLocation VANILLA_BACKGROUND_SPRITES = ReliableEmi.res("widget/scrollbar_background_vanilla");

    private boolean isDragging = false;

    public ScrollbarWidget(EmiScreenManager.SidebarPanel panel) {
        super(0, 0, 0, 0, Component.literal(""));
        this.panel = panel;
    }

    @Override
    public final void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            RenderSystem.enableBlend();
            SidebarPanelWithScrollOffset scrollPanel = (SidebarPanelWithScrollOffset) panel;

            int ENTRY_SIZE = 18;
            int SUBPANEL_SEPERATOR_SIZE = 3;
            int x = this.getX();
            int y = this.getY();
            int panelPadding = 0;
            int trackPadding = 0;
            int trackHeight = height;

            if (panel.theme == SidebarTheme.VANILLA) {
                panelPadding = 1;
                trackPadding = 1 + panelPadding;
                int headerOffset = panel.header ? 18 : 0;
                int panelHeight = panel.theme.verticalPadding * 2 - SUBPANEL_SEPERATOR_SIZE;
                for (EmiScreenManager.ScreenSpace space : panel.getSpaces()) {
                    panelHeight += space.th * ENTRY_SIZE + SUBPANEL_SEPERATOR_SIZE;
                }

                trackHeight += trackPadding * 2;

                guiGraphics.blitSprite(VANILLA_BACKGROUND_SPRITES, x, y - headerOffset - panel.theme.verticalPadding, width, panelHeight + headerOffset);
                guiGraphics.blitSprite(VANILLA_TRACK_SPRITES, x, y - trackPadding, width, trackHeight);
            } else {
                guiGraphics.blitSprite(TRACK_SPRITES, x, y - trackPadding, width, trackHeight);

            }
            int progress = scrollPanel.remi$getScrollOffsetRows();
            int totalScrollRows = scrollPanel.remi$getTotalScrollRows();
            int total = totalScrollRows + panel.space.th;

            if (totalScrollRows == 0) {
                return;
            }

            double segment = (double) trackHeight / total;
            int start = (int) (y + segment * progress) - panelPadding;
            int end = (int) (start + segment * panel.space.th);

            if (progress == totalScrollRows) {
                end = y + trackHeight - panelPadding - trackPadding;
                start = (int) (end - Math.max(segment * panel.space.th, 1)) - panelPadding;
            }

            if (panel.theme == SidebarTheme.VANILLA) {
                guiGraphics.blitSprite(VANILLA_THUMB_SPRITES, x, start, width, end - start);
            } else {
                guiGraphics.blitSprite(THUMB_SPRITES, x, start, width, end - start);
            }

            RenderSystem.disableBlend();
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        updateScrollPosition(mouseY);
        this.isDragging = true;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        if (this.isDragging) {
            updateScrollPosition(mouseY);
        }
    }

    private void updateScrollPosition(double mouseY) {
        SidebarPanelWithScrollOffset scrollPanel = (SidebarPanelWithScrollOffset) this.panel;

        int totalScrollRows = scrollPanel.remi$getTotalScrollRows();
        int thumbHeight = (int) (((double) height / (totalScrollRows + panel.space.th)) * panel.space.th);

        int localY = (int) mouseY - this.getY() - thumbHeight / 2;
        int trackHeight = height - thumbHeight;
        double fraction = (double) localY / trackHeight;
        int scrollOffset = (int) Math.round(fraction * totalScrollRows);

        scrollPanel.remi$setScrollOffset(scrollOffset);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public void stopDragging() {
        this.isDragging = false;
    }
}
