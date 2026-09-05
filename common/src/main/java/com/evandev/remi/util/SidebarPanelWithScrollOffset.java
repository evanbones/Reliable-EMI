package com.evandev.remi.util;

import com.evandev.remi.gui.components.ScrollbarWidget;

public interface SidebarPanelWithScrollOffset {
    int remi$getScrollOffset();

    void remi$setScrollOffset(int offset);

    int remi$getScrollOffsetRows();

    int remi$getTotalScrollRows();

    ScrollbarWidget remi$getScrollbarWidget();
}
