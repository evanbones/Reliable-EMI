package com.evandev.remi.feature.creativemodetab.gui;

import com.evandev.ReliableEmi;
import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.remi.feature.creativemodetab.gui.itemtab.ItemTabManager;
import com.evandev.remi.feature.creativemodetab.gui.itemtab.ItemTabNavigationBar;
import com.evandev.remi.gui.components.ImageButton;
import com.evandev.remi.gui.components.ScrollbarWidget;
import com.evandev.remi.integration.emi.ScreenManager;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.config.SidebarTheme;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import static com.evandev.remi.feature.creativemodetab.CreativeModeTabManager.getTotalTabCount;

public class CreativeModeTabGui {
    private static final int EMI_HEADER_HEIGHT = 18;
    private static final int BUTTON_SCROLL_OFFSET_Y = 2;
    private static final int BUTTON_SCROLL_OFFSET_X = 2;

    private static final ImageButton buttonScrollDown;
    private static final ImageButton buttonScrollUp;
    private static final ImageButton buttonPrevious;
    private static final ImageButton buttonNext;
    public static int tabCount = 0;
    public static boolean showScrollButtons = false;
    private static Screen screen;
    private static final ItemTabManager tabManager = new ItemTabManager(
            it -> screen.addRenderableWidget(it),
            it -> screen.removeWidget(it));
    public static final ItemTabNavigationBar topTabNavigationBar = new ItemTabNavigationBar(tabManager, false, false);
    public static final ItemTabNavigationBar leftTabNavigationBar = new ItemTabNavigationBar(tabManager, true, false);
    public static final ItemTabNavigationBar rightTabNavigationBar = new ItemTabNavigationBar(tabManager, true, true);
    private static double scrollAccumulator = 0.0;

    static {
        tabManager.setOnTabSelectedListener(CreativeModeTabManager::onTabSelected);
    }

    static {
        buttonPrevious = new ImageButton(12, 12, 0, 0, () -> CreativeModeTabManager.scrollOffset > 0,
                b -> CreativeModeTabManager.previousPage());
        buttonPrevious.matchScreenManagerVisibility();
        buttonPrevious.withTexture(ReliableEmi.res("textures/gui/scroll_left.png"), 12, 36);
        buttonNext = new ImageButton(12, 12, 0, 0, () -> CreativeModeTabManager.scrollOffset < CreativeModeTabManager.getMaxScroll(),
                b -> CreativeModeTabManager.nextPage());
        buttonNext.matchScreenManagerVisibility();
        buttonNext.withTexture(ReliableEmi.res("textures/gui/scroll_right.png"), 12, 36);
        buttonScrollDown = new ImageButton(12, 12, 0, 0, () -> CreativeModeTabManager.scrollOffset < CreativeModeTabManager.getMaxScroll(),
                b -> CreativeModeTabManager.nextPage());
        buttonScrollDown.matchScreenManagerVisibility();
        buttonScrollDown.withTexture(ReliableEmi.res("textures/gui/scroll_down.png"), 12, 36);
        buttonScrollUp = new ImageButton(12, 12, 0, 0, () -> CreativeModeTabManager.scrollOffset > 0,
                b -> CreativeModeTabManager.previousPage());
        buttonScrollUp.matchScreenManagerVisibility();
        buttonScrollUp.withTexture(ReliableEmi.res("textures/gui/scroll_up.png"), 12, 36);
    }

    public static TabTheme currentTheme() {
        if (ReliableEmiConfig.creativeTabTheme == ReliableEmiConfig.CreativeTabTheme.HORIZONTAL) {
            return TabTheme.HORIZONTAL;
        }
        if (ReliableEmiConfig.creativeTabTheme == ReliableEmiConfig.CreativeTabTheme.VERTICAL) {
            return TabTheme.VERTICAL;
        }
        var panel = ScreenManager.getTargetCreativeTabPanel();
        SidebarTheme theme = panel != null ? themeForSide(panel.side) : EmiConfig.rightSidebarTheme;
        return theme == SidebarTheme.VANILLA ? TabTheme.VERTICAL : TabTheme.HORIZONTAL;
    }

    private static SidebarTheme themeForSide(SidebarSide side) {
        return switch (side) {
            case LEFT -> EmiConfig.leftSidebarTheme;
            case RIGHT, NONE -> EmiConfig.rightSidebarTheme;
            case TOP -> EmiConfig.topSidebarTheme;
            case BOTTOM -> EmiConfig.bottomSidebarTheme;
        };
    }

    public static void onLayout() {
        var indexScreenSpace = ScreenManager.getActiveCreativeTabScreenSpace();
        var panel = ScreenManager.getTargetCreativeTabPanel();
        SidebarTheme panelTheme = panel != null ? panel.theme : SidebarTheme.TRANSPARENT;
        TabTheme theme = currentTheme();
        boolean hasHeader = panel != null && panel.header;
        int totalTabCount = getTotalTabCount();

        buttonPrevious.visible = false;
        buttonNext.visible = false;
        buttonScrollDown.visible = false;
        buttonScrollUp.visible = false;
        topTabNavigationBar.visible = false;
        leftTabNavigationBar.visible = false;
        rightTabNavigationBar.visible = false;
        tabCount = ReliableEmiConfig.maxSidebarTabs;
        showScrollButtons = false;

        if (indexScreenSpace == null) return;

        if (theme == TabTheme.HORIZONTAL) {
            int startX = indexScreenSpace.tx - panelTheme.horizontalPadding;
            int startY = indexScreenSpace.ty - (hasHeader ? EMI_HEADER_HEIGHT : 0) - ReliableEmiConfig.horizontalTabsHeight - panelTheme.verticalPadding;

            int availableWidth = indexScreenSpace.tw * ScreenManager.ENTRY_SIZE + panelTheme.horizontalPadding * 2;
            int availableWidthWhenScrolling = availableWidth - (buttonScrollUp.getHeight() + buttonScrollDown.getHeight() + BUTTON_SCROLL_OFFSET_Y * 2);

            if (panelTheme == SidebarTheme.VANILLA && ReliableEmiConfig.verticalScrollbar) {
                availableWidth += ScrollbarWidget.WIDTH - panelTheme.horizontalPadding;
                availableWidthWhenScrolling += ScrollbarWidget.WIDTH - panelTheme.horizontalPadding;
            }

            if (ReliableEmiConfig.maxSidebarTabs == 0) {
                tabCount = Math.clamp(availableWidth / ReliableEmiConfig.horizontalTabsWidth, 1, totalTabCount);

                if (totalTabCount > tabCount) {
                    tabCount = Math.max(1, availableWidthWhenScrolling / ReliableEmiConfig.horizontalTabsWidth);
                }
            }

            if (totalTabCount > tabCount) {
                startX += buttonPrevious.getWidth() + BUTTON_SCROLL_OFFSET_X;
                showScrollButtons = true;
                availableWidth = availableWidthWhenScrolling;
            }

            int tabBarWidth = tabCount * ReliableEmiConfig.horizontalTabsWidth;
            if (ReliableEmiConfig.tabAlignment == TabAlignment.STRETCH) {
                tabBarWidth = availableWidth;
            } else if (ReliableEmiConfig.tabAlignment == TabAlignment.END) {
                startX += availableWidth - tabBarWidth;
            } else if (ReliableEmiConfig.tabAlignment == TabAlignment.MIDDLE) {
                startX += (availableWidth - tabBarWidth) / 2;
            }

            topTabNavigationBar.setWidth(tabBarWidth);
            topTabNavigationBar.visible = true;
            topTabNavigationBar.pos(startX, startY);
            if (showScrollButtons) {
                int buttonY = startY + (ReliableEmiConfig.horizontalTabsHeight - 12 + panelTheme.verticalPadding / 2) / 2;
                buttonPrevious.visible = true;
                buttonPrevious.setX(startX - buttonPrevious.getWidth() - BUTTON_SCROLL_OFFSET_X);
                buttonPrevious.setY(buttonY);
                buttonNext.visible = true;
                buttonNext.setX(startX + tabBarWidth + BUTTON_SCROLL_OFFSET_X);
                buttonNext.setY(buttonY);
            }
        } else if (theme == TabTheme.VERTICAL){
            int headerHeight = (hasHeader && panelTheme == SidebarTheme.VANILLA ? EMI_HEADER_HEIGHT : 0) + panelTheme.verticalPadding;
            int startY = indexScreenSpace.ty - headerHeight;
            int leftX = indexScreenSpace.tx - ReliableEmiConfig.verticalTabsWidth - panelTheme.horizontalPadding;

            int availableHeight = headerHeight + panelTheme.verticalPadding - 3;
            if (panel != null) {
                for (EmiScreenManager.ScreenSpace space : panel.getSpaces()) {
                    availableHeight += space.th * ScreenManager.ENTRY_SIZE + 3;
                }
            }

            int availableHeightWhenScrolling = availableHeight - (buttonScrollUp.getHeight() + buttonScrollDown.getHeight() + BUTTON_SCROLL_OFFSET_Y * 2);

            if (ReliableEmiConfig.maxSidebarTabs == 0) {
                tabCount = Math.clamp(availableHeight / ReliableEmiConfig.verticalTabsHeight, 1, totalTabCount);

                if (totalTabCount > tabCount) {
                    tabCount = Math.max(1, availableHeightWhenScrolling / ReliableEmiConfig.verticalTabsHeight);
                }
            }

            if (totalTabCount > tabCount) {
                startY += buttonScrollUp.getHeight() + BUTTON_SCROLL_OFFSET_Y;
                showScrollButtons = true;
                availableHeight = availableHeightWhenScrolling;
            }

            int tabBarHeight = tabCount * ReliableEmiConfig.verticalTabsHeight;

            if (ReliableEmiConfig.tabAlignment == TabAlignment.STRETCH) {
                tabBarHeight = availableHeight;
            } else if (ReliableEmiConfig.tabAlignment == TabAlignment.END) {
                startY += availableHeight - tabBarHeight;
            } else if (ReliableEmiConfig.tabAlignment == TabAlignment.MIDDLE) {
                startY += (availableHeight - tabBarHeight) / 2;
            }

            leftTabNavigationBar.setHeight(tabBarHeight);

            if (showScrollButtons) {
                buttonScrollUp.visible = true;
                buttonScrollUp.setX(leftX + (ReliableEmiConfig.verticalTabsWidth - buttonScrollUp.getWidth() + (panelTheme.horizontalPadding / 2)) / 2);
                buttonScrollUp.setY(startY - buttonScrollUp.getHeight() - BUTTON_SCROLL_OFFSET_Y);
                buttonScrollDown.visible = true;
                buttonScrollDown.setX(leftX + (ReliableEmiConfig.verticalTabsWidth - buttonScrollUp.getWidth() + (panelTheme.horizontalPadding / 2)) / 2);
                buttonScrollDown.setY(startY + tabBarHeight + BUTTON_SCROLL_OFFSET_Y);
            }

            leftTabNavigationBar.visible = true;
            leftTabNavigationBar.pos(leftX, startY);
        }
    }

    public static void initialize(Screen s) {
        screen = s;
        screen.removeWidget(buttonPrevious);
        screen.removeWidget(buttonNext);
        screen.removeWidget(buttonScrollDown);
        screen.removeWidget(buttonScrollUp);
        screen.removeWidget(topTabNavigationBar);
        screen.removeWidget(leftTabNavigationBar);
        screen.removeWidget(rightTabNavigationBar);

        screen.addRenderableWidget(buttonPrevious);
        screen.addRenderableWidget(buttonNext);
        screen.addRenderableWidget(buttonScrollDown);
        screen.addRenderableWidget(buttonScrollUp);
        screen.addRenderableWidget(topTabNavigationBar);
        screen.addRenderableWidget(leftTabNavigationBar);
        screen.addRenderableWidget(rightTabNavigationBar);
        onLayout();
    }

    public static boolean contains(double mouseX, double mouseY) {
        return checkBar(topTabNavigationBar, mouseX, mouseY)
                || checkBar(leftTabNavigationBar, mouseX, mouseY)
                || checkBar(rightTabNavigationBar, mouseX, mouseY);
    }

    private static boolean checkBar(ItemTabNavigationBar bar, double mouseX, double mouseY) {
        int mx = (int) mouseX, my = (int) mouseY;
        return bar.visible && mx >= bar.getX() && mx <= bar.getX() + bar.getWidth()
                && my >= bar.getY() && my <= bar.getY() + bar.getHeight();
    }

    public static boolean onMouseScrolled(double amount) {
        scrollAccumulator += amount;
        int sa = (int) scrollAccumulator;
        scrollAccumulator %= 1;
        if (sa > 0) CreativeModeTabManager.previousPage();
        else if (sa < 0) CreativeModeTabManager.nextPage();
        return true;
    }

    public static void selectTab(int tabIndex, boolean playClickSound) {
        ItemTabNavigationBar targetBar = currentTheme() == TabTheme.VERTICAL
                ? leftTabNavigationBar : topTabNavigationBar;
        if (tabIndex >= 0 && tabIndex < targetBar.tabButtons.size()) {
            var selectedButton = targetBar.tabButtons.get(tabIndex);
            targetBar.setFocusedChild(selectedButton);
            if (playClickSound) Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    public enum TabTheme {HORIZONTAL, VERTICAL}
    public enum TabAlignment {START, MIDDLE, END, STRETCH}
}