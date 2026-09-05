package com.evandev.remi.gui;

import com.evandev.ReliableEmi;
import com.evandev.remi.gui.components.Switch;
import com.evandev.remi.integration.emi.ScreenManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ListEntry extends AbstractWidget implements ContainerEventHandler {
    protected static final int PADDING = 8;
    protected static final int BORDER_WIDTH = 1;
    public static final int WIDTH = ScreenManager.ENTRY_SIZE * 8 + PADDING * 2 + BORDER_WIDTH * 2;
    public static final int HEIGHT = ScreenManager.ENTRY_SIZE * 2 + PADDING * 2 + BORDER_WIDTH * 2;
    private static final ResourceLocation BACKGROUND = ReliableEmi.res("textures/gui/inworld_menu_list_background.png");
    private static final int SEPARATOR_COLOR = 0x66000000;
    private final GuiEventListener container;
    private boolean dragging;
    @Nullable
    private GuiEventListener focusedChild;

    protected ListEntry(GuiEventListener container) {
        super(0, 0, WIDTH, HEIGHT, Component.empty());
        this.container = container;
    }

    protected abstract Switch getSwitch();

    protected abstract List<AbstractWidget> getChildren();

    public abstract boolean shouldRenderSwitch();

    public abstract Component getEntryTitle();

    public abstract void renderEntry(GuiGraphics guiGraphics, int mouseX, int mouseY, int startX, int startY, float partialTick);

    public void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }

    public int getRight() {
        return getX() + getWidth();
    }

    public int getBottom() {
        return getY() + getHeight();
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int startX = getX() + BORDER_WIDTH + PADDING;
        int startY = getY() + BORDER_WIDTH + PADDING;
        renderEntryBackground(guiGraphics);
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
        guiGraphics.fill(getX(), getY(), getRight(), getY() + 2, SEPARATOR_COLOR);
        guiGraphics.fill(getX(), getBottom() - 2, getRight(), getBottom(), SEPARATOR_COLOR);
    }

    private void renderEntryBackground(GuiGraphics guiGraphics) {
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
    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return getChildren();
    }

    @Override
    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void setDragging(boolean isDragging) {
        this.dragging = isDragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return focusedChild;
    }

    @Override
    public void setFocused(boolean isFocused) {
        if (!isFocused) getChildren().forEach(w -> w.setFocused(false));
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (focusedChild != null) focusedChild.setFocused(false);
        if (focused != null) focused.setFocused(true);
        this.focusedChild = focused;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return ContainerEventHandler.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return ContainerEventHandler.super.nextFocusPath(event);
    }
}
