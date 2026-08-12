package com.evandev.remi.feature.creativemodetab.gui;

import com.evandev.ReliableEmi;
import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.remi.feature.creativemodetab.gui.itemtab.ItemTabManager;
import com.evandev.remi.feature.creativemodetab.gui.itemtab.ItemTabNavigationBar;
import com.evandev.remi.gui.components.ImageButton;
import com.evandev.remi.integration.emi.ScreenManager;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.HeaderType;
import dev.emi.emi.config.SidebarTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class CreativeModeTabGui {
    public static final int CREATIVE_MODE_TAB_HEIGHT = 18;
    public static final int VERTICAL_TAB_WIDTH = 35;

    private static final int EMI_HEADER_HEIGHT = 18;
    private static final int VANILLA_TAB_HEIGHT = 27;
    private static final int VERTICAL_MARGIN = 8;
    private static final int HORIZONTAL_OFFSET = 8;
    private static final int BUTTON_SCROLL_OFFSET_X = 13;
    private static final int BUTTON_SCROLL_OFFSET_Y = 6;
    private static final int MAX_TAB_COUNT_DEFAULT = 254;
    private static final int MAX_TAB_COUNT_VANILLA = 11;

    private static final ImageButton buttonScrollDown;
    private static final ImageButton buttonScrollUp;
    public static int tabCount = 0;
    private static Screen screen;
    private static final ItemTabManager tabManager = new ItemTabManager(
            it -> screen.addRenderableWidget(it),
            it -> screen.removeWidget(it));
    public static final ItemTabNavigationBar topTabNavigationBar = new ItemTabNavigationBar(tabManager, false, false);
    public static final ItemTabNavigationBar leftTabNavigationBar = new ItemTabNavigationBar(tabManager, true, false);
    private static final ImageButton buttonPrevious = new ImageButton(16, 16, 0, 0, () -> true,
            b -> CreativeModeTabManager.previousPage());
    private static final ImageButton buttonNext = new ImageButton(16, 16, 16, 0, () -> true,
            b -> CreativeModeTabManager.nextPage());
    public static final ItemTabNavigationBar rightTabNavigationBar = new ItemTabNavigationBar(tabManager, true, true);
    private static double scrollAccumulator = 0.0;

    static {
        tabManager.setOnTabSelectedListener(CreativeModeTabManager::onTabSelected);
    }

    static {
        buttonPrevious.matchScreenManagerVisibility();
        buttonNext.matchScreenManagerVisibility();
        buttonScrollDown = new ImageButton(8, 4, 0, 0, () -> CreativeModeTabManager.scrollOffset < CreativeModeTabManager.getMaxScroll(),
                b -> CreativeModeTabManager.nextPage());
        buttonScrollDown.matchScreenManagerVisibility();
        buttonScrollDown.withTexture(ReliableEmi.res("textures/gui/scroll_down.png"), 8, 8);
        buttonScrollUp = new ImageButton(8, 4, 0, 0, () -> CreativeModeTabManager.scrollOffset > 0,
                b -> CreativeModeTabManager.previousPage());
        buttonScrollUp.matchScreenManagerVisibility();
        buttonScrollUp.withTexture(ReliableEmi.res("textures/gui/scroll_up.png"), 8, 8);
    }

    public static TabTheme currentTheme() {
        if (ReliableEmiConfig.creativeTabTheme == ReliableEmiConfig.CreativeTabTheme.MODERN) {
            return TabTheme.DEFAULT;
        }
        if (ReliableEmiConfig.creativeTabTheme == ReliableEmiConfig.CreativeTabTheme.VANILLA) {
            return TabTheme.VANILLA;
        }
        var panel = ScreenManager.getTargetCreativeTabPanel();
        if (panel != null) {
            return panel.theme == SidebarTheme.VANILLA ? TabTheme.VANILLA : TabTheme.DEFAULT;
        }
        return EmiConfig.rightSidebarTheme == SidebarTheme.VANILLA ? TabTheme.VANILLA : TabTheme.DEFAULT;
    }

    private static boolean isHeaderVisible() {
        var panel = ScreenManager.getTargetCreativeTabPanel();
        if (panel != null) {
            return panel.header;
        }
        return EmiConfig.rightSidebarHeader == HeaderType.VISIBLE;
    }

    public static void onLayout() {
        var indexScreenSpace = ScreenManager.getActiveCreativeTabScreenSpace();
        TabTheme theme = currentTheme();

        buttonPrevious.visible = false;
        buttonNext.visible = false;
        buttonScrollDown.visible = false;
        buttonScrollUp.visible = false;
        topTabNavigationBar.visible = false;
        leftTabNavigationBar.visible = false;
        rightTabNavigationBar.visible = false;

        if (indexScreenSpace == null) return;

        if (theme == TabTheme.DEFAULT) {
            int startX = indexScreenSpace.tx;
            int startY = indexScreenSpace.ty - (isHeaderVisible() ? EMI_HEADER_HEIGHT : 0) - CREATIVE_MODE_TAB_HEIGHT;
            int tileW = indexScreenSpace.tw;
            tabCount = ReliableEmiConfig.maxSidebarTabs > 0 ? ReliableEmiConfig.maxSidebarTabs : Math.max(1, Math.min(MAX_TAB_COUNT_DEFAULT, tileW - 2));

            buttonPrevious.visible = true;
            buttonPrevious.setX(startX);
            buttonPrevious.setY(startY + 2);

            topTabNavigationBar.visible = true;
            topTabNavigationBar.pos(startX + buttonPrevious.getWidth(), startY);

            buttonNext.visible = true;
            buttonNext.setX(topTabNavigationBar.getX() + topTabNavigationBar.getWidth());
            buttonNext.setY(startY + 2);
        } else {
            int startY = indexScreenSpace.ty - VANILLA_TAB_HEIGHT;
            int leftX = indexScreenSpace.tx - VERTICAL_TAB_WIDTH - VERTICAL_MARGIN + HORIZONTAL_OFFSET;

            if (theme == TabTheme.VANILLA) {
                int availableHeight = (indexScreenSpace.th * ScreenManager.ENTRY_SIZE) + (VANILLA_TAB_HEIGHT - 1);
                tabCount = ReliableEmiConfig.maxSidebarTabs > 0 ? ReliableEmiConfig.maxSidebarTabs : Math.max(1, availableHeight / VANILLA_TAB_HEIGHT);
                leftTabNavigationBar.visible = true;
                leftTabNavigationBar.pos(leftX, startY);
                buttonScrollUp.visible = true;
                buttonScrollUp.setX(leftX + BUTTON_SCROLL_OFFSET_X);
                buttonScrollUp.setY(startY - BUTTON_SCROLL_OFFSET_Y - 4);
                buttonScrollDown.visible = true;
                buttonScrollDown.setX(leftX + BUTTON_SCROLL_OFFSET_X);
                buttonScrollDown.setY(startY + (tabCount * VANILLA_TAB_HEIGHT) + BUTTON_SCROLL_OFFSET_Y);
            } else {
                tabCount = ReliableEmiConfig.maxSidebarTabs > 0 ? ReliableEmiConfig.maxSidebarTabs : MAX_TAB_COUNT_VANILLA;
                leftTabNavigationBar.visible = true;
                leftTabNavigationBar.pos(leftX, startY);
            }
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
        ItemTabNavigationBar targetBar = currentTheme() == TabTheme.VANILLA
                ? leftTabNavigationBar : topTabNavigationBar;
        if (tabIndex >= 0 && tabIndex < targetBar.tabButtons.size()) {
            var selectedButton = targetBar.tabButtons.get(tabIndex);
            targetBar.setFocusedChild(selectedButton);
            if (playClickSound) Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    public enum TabTheme {DEFAULT, VANILLA}
}