package com.evandev.emixx.gui;

import com.evandev.emixx.integration.emi.ScreenManager;
import com.evandev.emixx.gui.components.Switch;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ListEntry extends AbstractContainerWidget {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    private static final int PADDING = 8;
    private static final int BORDER_WIDTH = 1;
    public static final int WIDTH = ScreenManager.ENTRY_SIZE * 8 + PADDING * 2 + BORDER_WIDTH * 2;
    public static final int HEIGHT = ScreenManager.ENTRY_SIZE * 2 + PADDING * 2 + BORDER_WIDTH * 2;

    private final GuiEventListener container;

    protected ListEntry(GuiEventListener container) {
        super(0, 0, WIDTH, HEIGHT, Component.empty());
        this.container = container;
    }

    protected abstract Switch getSwitch();

    protected abstract List<AbstractWidget> getChildren();

    public abstract boolean shouldRenderSwitch();

    public abstract Component getEntryTitle();

    public abstract void renderEntry(GuiGraphics guiGraphics, int mouseX, int mouseY, int startX, int startY, float partialTick);

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int startX = getX() + BORDER_WIDTH + PADDING;
        int startY = getY() + BORDER_WIDTH + PADDING;
        renderBackground(guiGraphics);
        renderBorders(guiGraphics);

        Component title = getEntryTitle();
        if (title != null) {
            int maxWidth = WIDTH - BORDER_WIDTH - PADDING - 46;
            var font = Minecraft.getInstance().font;
            if (font.width(title) < maxWidth) {
                guiGraphics.drawString(font, title, startX, startY + 2, 0xFFFFFF);
            } else {
                renderScrollingString(guiGraphics, font, title, startX, startY, startX + maxWidth, startY + 2 + font.lineHeight, 0xFFFFFF);
            }
        }

        renderEntry(guiGraphics, mouseX, mouseY, startX, startY, partialTick);

        if (shouldRenderSwitch()) {
            Switch sw = getSwitch();
            sw.setX(getX() + WIDTH - sw.getWidth() - BORDER_WIDTH - PADDING);
            sw.setY(startY);
            sw.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderBorders(GuiGraphics guiGraphics) {
        RenderSystem.enableBlend();
        guiGraphics.blit(Screen.INWORLD_HEADER_SEPARATOR, getX(), getY(), 0f, 0f, getWidth(), 2, 32, 2);
        guiGraphics.blit(Screen.INWORLD_FOOTER_SEPARATOR, getX(), getBottom(), 0f, 0f, getWidth(), 2, 32, 2);
        RenderSystem.disableBlend();
    }

    private void renderBackground(GuiGraphics guiGraphics) {
        RenderSystem.enableBlend();
        guiGraphics.blit(BACKGROUND, getX(), getY(), getRight(), getBottom(), getWidth(), getHeight(), 32, 32);
        RenderSystem.disableBlend();
    }

    @Override
    public boolean isFocused() {
        if (container instanceof ContainerEventHandler ceh)
            return ceh.getFocused() == this;
        return false;
    }

    @Override
    public void setFocused(boolean isFocused) {
        if (!isFocused) getChildren().forEach(w -> w.setFocused(false));
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return getChildren();
    }
}
