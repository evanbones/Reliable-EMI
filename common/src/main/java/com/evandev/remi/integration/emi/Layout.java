package com.evandev.remi.integration.emi;

import com.evandev.remi.feature.stackgroup.StackGroupManager;
import com.evandev.remi.feature.stackgroup.data.StackGroup;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;

public class Layout {
    private static final int HIGHLIGHT_COLOR = 0x66FFFFFF;
    private static final int TILE_OFFSET_ADJUST = 17;

    public static int startIndex = -10;
    public static boolean clean = true;
    public static boolean textureDirty = true;

    public static void checkGridSize(int tw, int th) {
        if (tw <= 0 || th <= 0) {
            StackManager.stackGrid = new EmiStack[0][0];
            clean = false;
            return;
        }
        EmiStack[][] grid = StackManager.stackGrid;
        if (grid.length < th || grid[0].length < tw) {
            StackManager.stackGrid = new EmiStack[th][tw];
            clean = false;
        }
    }

    public static void buildLayoutTiles(EmiScreenManager.ScreenSpace screenSpace, EmiDrawContext context) {
        if (textureDirty) {
            StackManager.stackTextureGrid.clear();
            for (int y = 0; y < screenSpace.th; y++) {
                for (int x = 0; x < screenSpace.tw; x++) {
                    EmiStack emiStack = getFromGrid(y, x);
                    StackGroup currentGroup = getGroup(emiStack);
                    if (emiStack == null || currentGroup == null) continue;

                    Tile tile = new Tile(x, y, 0);
                    if (y == 0 || groupAt(y - 1, x) != currentGroup) tile.type |= TileType.TOP.bit;
                    if (x == 0 || groupAt(y, x - 1) != currentGroup) tile.type |= TileType.LEFT.bit;
                    if (y == screenSpace.th - 1 || groupAt(y + 1, x) != currentGroup) tile.type |= TileType.BOTTOM.bit;
                    if (x == screenSpace.tw - 1 || groupAt(y, x + 1) != currentGroup) tile.type |= TileType.RIGHT.bit;

                    if (groupAt(y - 1, x - 1) != currentGroup && groupAt(y - 1, x) == currentGroup && groupAt(y, x - 1) == currentGroup)
                        tile.type |= TileType.TOP_LEFT.bit;
                    if (groupAt(y - 1, x + 1) != currentGroup && groupAt(y - 1, x) == currentGroup && groupAt(y, x + 1) == currentGroup)
                        tile.type |= TileType.TOP_RIGHT.bit;
                    if (groupAt(y + 1, x - 1) != currentGroup && groupAt(y + 1, x) == currentGroup && groupAt(y, x - 1) == currentGroup)
                        tile.type |= TileType.BOTTOM_LEFT.bit;
                    if (groupAt(y + 1, x + 1) != currentGroup && groupAt(y + 1, x) == currentGroup && groupAt(y, x + 1) == currentGroup)
                        tile.type |= TileType.BOTTOM_RIGHT.bit;

                    if (tile.type != 0) StackManager.stackTextureGrid.add(tile);
                }
            }
        }
        textureDirty = false;
        render(screenSpace, context);
    }

    public static void render(EmiScreenManager.ScreenSpace screenSpace, EmiDrawContext context) {
        int es = ScreenManager.ENTRY_SIZE;
        for (Tile tile : StackManager.stackTextureGrid) {
            int px = screenSpace.tx + tile.x * es;
            int py = screenSpace.ty + tile.y * es;
            if (tile.check(TileType.TOP)) context.fill(px, py, es, 1, HIGHLIGHT_COLOR);
            if (tile.check(TileType.LEFT)) context.fill(px, py, 1, es, HIGHLIGHT_COLOR);
            if (tile.check(TileType.BOTTOM)) context.fill(px, py + TILE_OFFSET_ADJUST, es, 1, HIGHLIGHT_COLOR);
            if (tile.check(TileType.RIGHT)) context.fill(px + TILE_OFFSET_ADJUST, py, 1, es, HIGHLIGHT_COLOR);
            if (tile.check(TileType.TOP_LEFT)) context.fill(px, py, 1, 1, HIGHLIGHT_COLOR);
            if (tile.check(TileType.TOP_RIGHT)) context.fill(px + es - 1, py, 1, 1, HIGHLIGHT_COLOR);
            if (tile.check(TileType.BOTTOM_LEFT)) context.fill(px, py + es - 1, 1, 1, HIGHLIGHT_COLOR);
            if (tile.check(TileType.BOTTOM_RIGHT)) context.fill(px + es - 1, py + es - 1, 1, 1, HIGHLIGHT_COLOR);
        }
    }

    private static EmiStack getFromGrid(int y, int x) {
        EmiStack[][] grid = StackManager.stackGrid;
        if (grid == null || y < 0 || y >= grid.length || grid[y] == null || x < 0 || x >= grid[y].length) return null;
        return grid[y][x];
    }

    private static StackGroup getGroup(EmiStack emiStack) {
        if (emiStack == null) return null;
        var groupedStacks = StackGroupManager.stackToGroupedStacks.get(emiStack);
        if (groupedStacks == null) return null;
        for (var gs : groupedStacks) {
            if (StackManager.expandedStackGroups.contains(gs.stackGroup.getId())) return gs.stackGroup;
        }
        return null;
    }

    private static StackGroup groupAt(int y, int x) {
        return getGroup(getFromGrid(y, x));
    }

    public enum TileType {
        LEFT(1), TOP(2), RIGHT(4), BOTTOM(8),
        TOP_LEFT(16), TOP_RIGHT(32), BOTTOM_LEFT(64), BOTTOM_RIGHT(128);
        public final int bit;

        TileType(int bit) {
            this.bit = bit;
        }
    }

    public static class Tile {
        public final int x, y;
        public int type;

        public Tile(int x, int y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        public boolean check(TileType bit) {
            return (type & bit.bit) == bit.bit;
        }
    }
}