package com.evandev.remi.feature.creativemodetab.gui.itemtab;

import com.evandev.ReliableEmi;
import com.evandev.remi.integration.emi.ScreenManager;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemTabNavigationBar extends AbstractWidget {
    private static final ResourceLocation TEXTURE = ReliableEmi.res("textures/gui/buttons.png");

    private final ItemTabManager tabManager;
    private final boolean isVertical;
    private final boolean isRightSide;
    public List<TabButton> tabButtons = new ArrayList<>();
    public List<ItemTab> visibleTabs = new ArrayList<>();
    private GridLayout layout = new GridLayout();
    private GuiEventListener focusedChild;

    public ItemTabNavigationBar(ItemTabManager tabManager, boolean isVertical, boolean isRightSide) {
        super(0, 0, 0, 0, Component.empty());
        this.tabManager = tabManager;
        this.isVertical = isVertical;
        this.isRightSide = isRightSide;
    }

    public void pos(int x, int y) {
        this.setX(x);
        this.setY(y);
        arrangeElements();
    }

    public void setTabs(List<ItemTab> tabs) {
        this.visibleTabs = new ArrayList<>(tabs);
        GridLayout newLayout = new GridLayout();
        newLayout.defaultCellSetting().padding(0);

        ImmutableList.Builder<TabButton> buttonBuilder = ImmutableList.builder();
        for (int i = 0; i < tabs.size(); i++) {
            ItemTab tab = tabs.get(i);
            ItemTabButton.ButtonStyle buttonStyle = !isVertical ? ItemTabButton.ButtonStyle.TOP
                    : isRightSide ? ItemTabButton.ButtonStyle.RIGHT : ItemTabButton.ButtonStyle.LEFT;
            int w = isVertical ? 35 : ScreenManager.ENTRY_SIZE;
            int h = isVertical ? 27 : ScreenManager.ENTRY_SIZE;
            ItemTabButton button = new ItemTabButton(tabManager, tab, w, h, buttonStyle, i == 0);
            buttonBuilder.add(button);
            if (isVertical) newLayout.addChild(button, i, 0);
            else newLayout.addChild(button, 0, i);
        }
        this.tabButtons = buttonBuilder.build();
        this.layout = newLayout;
        arrangeElements();
    }

    public void arrangeElements() {
        if (!isVertical) {
            tabButtons.forEach(b -> b.setWidth(ScreenManager.ENTRY_SIZE));
        }
        layout.setX(isVertical ? getX() : getX() + 2);
        layout.setY(getY());
        layout.arrangeElements();
        if (isVertical) {
            width = 35;
        } else {
            width = layout.getWidth() + 4;
        }
        height = layout.getHeight();
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics raw, int mouseX, int mouseY, float partialTick) {
        if (EmiScreenManager.isDisabled()) return;
        if (!isVertical) {
            RenderSystem.enableBlend();
            EmiDrawContext ctx = EmiDrawContext.wrap(raw);
            ctx.drawTexture(TEXTURE, getX(), getY() + 2, 32, 0, 1, 16);
            ctx.drawTexture(TEXTURE, getX() + 1, getY() + 2, 32, 0, 1, 16);
            ctx.drawTexture(TEXTURE, getX() + width - 1, getY() + 2, 32, 0, 1, 16);
            ctx.drawTexture(TEXTURE, getX() + width - 2, getY() + 2, 32, 0, 1, 16);
            RenderSystem.disableBlend();
        }
        tabButtons.forEach(b -> b.render(raw, mouseX, mouseY, partialTick));
    }

    public void setFocusedChild(GuiEventListener child) {
        if (focusedChild != null) focusedChild.setFocused(false);
        focusedChild = child;
        if (child != null) {
            child.setFocused(true);
            if (child instanceof TabButton tb) {
                tabManager.setCurrentTab(tb.tab(), false);
            }
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) setFocusedChild(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (TabButton child : tabButtons) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                setFocusedChild(child);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }
}
