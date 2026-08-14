package com.evandev.remi.integration.emi;

import com.evandev.ReliableEmi;
import com.evandev.remi.feature.stackgroup.EmiGroupStack;
import com.evandev.remi.feature.stackgroup.GroupedEmiStack;
import com.evandev.remi.feature.stackgroup.StackGroupManager;
import com.evandev.remi.feature.stackgroup.data.StackGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class Layout {
    private static final ResourceLocation STACK_GROUP_TEXTURE = ReliableEmi.res("textures/gui/stack_group.png");
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
        SidebarType type = screenSpace.getType();
        List<Tile> tiles = new ArrayList<>();
        for (int y = 0; y < screenSpace.th; y++) {
            for (int x = 0; x < screenSpace.tw; x++) {
                EmiStack emiStack = getFromGrid(y, x);
                StackGroup currentGroup = getGroup(emiStack, type);
                if (emiStack == null || currentGroup == null) continue;

                Tile tile = new Tile(x, y, 0);
                if (groupAt(y - 1, x, type) == currentGroup) tile.type |= Connections.TOP.bit;
                if (groupAt(y, x - 1, type) == currentGroup) tile.type |= Connections.LEFT.bit;
                if (groupAt(y + 1, x, type) == currentGroup) tile.type |= Connections.BOTTOM.bit;
                if (groupAt(y, x + 1, type) == currentGroup) tile.type |= Connections.RIGHT.bit;
                if (groupAt(y - 1, x - 1, type) == currentGroup) tile.type |= Connections.TOP_LEFT.bit;
                if (groupAt(y - 1, x + 1, type) == currentGroup) tile.type |= Connections.TOP_RIGHT.bit;
                if (groupAt(y + 1, x - 1, type) == currentGroup) tile.type |= Connections.BOTTOM_LEFT.bit;
                if (groupAt(y + 1, x + 1, type) == currentGroup) tile.type |= Connections.BOTTOM_RIGHT.bit;

                tiles.add(tile);
            }
        }
        render(screenSpace, context, tiles);
    }

    public static void render(EmiScreenManager.ScreenSpace screenSpace, EmiDrawContext context, List<Tile> tiles) {
        int es = ScreenManager.ENTRY_SIZE;

        RenderSystem.enableBlend();
        for (Tile tile : tiles) {
            int px = screenSpace.tx + tile.x * es;
            int py = screenSpace.ty + tile.y * es;
            int[] uv = getTileUV(tile);
            context.drawTexture(STACK_GROUP_TEXTURE, px, py, 0, uv[0] * es, uv[1] * es, es, es, 144, 108);
        }
        RenderSystem.disableBlend();
    }

    protected static int[] getTileUV(Tile tile){
        int[] uv;

        /* UV Mapping adapted from Fusion (https://github.com/SuperMartijn642/Fusion) */
        if (!tile.check(Connections.LEFT) && !tile.check(Connections.TOP) && !tile.check(Connections.RIGHT) && !tile.check(Connections.BOTTOM))
            uv = new int[]{0, 0};
        else {
            if (tile.check(Connections.LEFT) && !tile.check(Connections.TOP) && !tile.check(Connections.RIGHT) && !tile.check(Connections.BOTTOM))
                uv = new int[]{3, 0};
            else if (!tile.check(Connections.LEFT) && tile.check(Connections.TOP) && !tile.check(Connections.RIGHT) && !tile.check(Connections.BOTTOM))
                uv = new int[]{0, 3};
            else if (!tile.check(Connections.LEFT) && !tile.check(Connections.TOP) && tile.check(Connections.RIGHT) && !tile.check(Connections.BOTTOM))
                uv = new int[]{1, 0};
            else if (!tile.check(Connections.LEFT) && !tile.check(Connections.TOP) && !tile.check(Connections.RIGHT) && tile.check(Connections.BOTTOM))
                uv = new int[]{0, 1};
            else {
                if (tile.check(Connections.LEFT) && !tile.check(Connections.TOP) && tile.check(Connections.RIGHT) && !tile.check(Connections.BOTTOM))
                    uv = new int[]{2, 0};
                else if (!tile.check(Connections.LEFT) && tile.check(Connections.TOP) && !tile.check(Connections.RIGHT) && tile.check(Connections.BOTTOM))
                    uv = new int[]{0, 2};
                else if (tile.check(Connections.LEFT) && tile.check(Connections.TOP) && !tile.check(Connections.RIGHT) && !tile.check(Connections.BOTTOM)) {
                    if (tile.check(Connections.TOP_LEFT))
                        uv = new int[]{3, 3};
                    else
                        uv = new int[]{5, 1};
                } else if (!tile.check(Connections.LEFT) && tile.check(Connections.TOP) && tile.check(Connections.RIGHT) && !tile.check(Connections.BOTTOM)) {
                    if (tile.check(Connections.TOP_RIGHT))
                        uv = new int[]{1, 3};
                    else
                        uv = new int[]{4, 1};
                } else if (!tile.check(Connections.LEFT) && !tile.check(Connections.TOP) && tile.check(Connections.RIGHT) && tile.check(Connections.BOTTOM)) {
                    if (tile.check(Connections.BOTTOM_RIGHT))
                        uv = new int[]{1, 1};
                    else
                        uv = new int[]{4, 0};
                } else if (tile.check(Connections.LEFT) && !tile.check(Connections.TOP) && !tile.check(Connections.RIGHT) && tile.check(Connections.BOTTOM)) {
                    if (tile.check(Connections.BOTTOM_LEFT))
                        uv = new int[]{3, 1};
                    else
                        uv = new int[]{5, 0};
                } else {
                    if (!tile.check(Connections.LEFT)) {
                        if (tile.check(Connections.TOP_RIGHT) && tile.check(Connections.BOTTOM_RIGHT))
                            uv = new int[]{1, 2};
                        else if (tile.check(Connections.TOP_RIGHT))
                            uv = new int[]{4, 2};
                        else if (tile.check(Connections.BOTTOM_RIGHT))
                            uv = new int[]{6, 2};
                        else
                            uv = new int[]{6, 0};
                    } else if (!tile.check(Connections.TOP)) {
                        if (tile.check(Connections.BOTTOM_LEFT) && tile.check(Connections.BOTTOM_RIGHT))
                            uv = new int[]{2, 1};
                        else if (tile.check(Connections.BOTTOM_LEFT))
                            uv = new int[]{7, 2};
                        else if (tile.check(Connections.BOTTOM_RIGHT))
                            uv = new int[]{5, 2};
                        else
                            uv = new int[]{7, 0};
                    } else if (!tile.check(Connections.RIGHT)) {
                        if (tile.check(Connections.TOP_LEFT) && tile.check(Connections.BOTTOM_LEFT))
                            uv = new int[]{3, 2};
                        else if (tile.check(Connections.TOP_LEFT))
                            uv = new int[]{7, 3};
                        else if (tile.check(Connections.BOTTOM_LEFT))
                            uv = new int[]{5, 3};
                        else
                            uv = new int[]{7, 1};
                    } else if (!tile.check(Connections.BOTTOM)) {
                        if (tile.check(Connections.TOP_LEFT) && tile.check(Connections.TOP_RIGHT))
                            uv = new int[]{2, 3};
                        else if (tile.check(Connections.TOP_LEFT))
                            uv = new int[]{4, 3};
                        else if (tile.check(Connections.TOP_RIGHT))
                            uv = new int[]{6, 3};
                        else
                            uv = new int[]{6, 1};
                    } else {
                        if (tile.check(Connections.TOP_LEFT) && tile.check(Connections.TOP_RIGHT) && tile.check(Connections.BOTTOM_LEFT) && tile.check(Connections.BOTTOM_RIGHT))
                            uv = new int[]{2, 2};
                        else {
                            if (!tile.check(Connections.TOP_LEFT) && tile.check(Connections.TOP_RIGHT) && tile.check(Connections.BOTTOM_LEFT) && tile.check(Connections.BOTTOM_RIGHT))
                                uv = new int[]{7, 5};
                            else if (tile.check(Connections.TOP_LEFT) && !tile.check(Connections.TOP_RIGHT) && tile.check(Connections.BOTTOM_LEFT) && tile.check(Connections.BOTTOM_RIGHT))
                                uv = new int[]{6, 5};
                            else if (tile.check(Connections.TOP_LEFT) && tile.check(Connections.TOP_RIGHT) && !tile.check(Connections.BOTTOM_LEFT) && tile.check(Connections.BOTTOM_RIGHT))
                                uv = new int[]{7, 4};
                            else if (tile.check(Connections.TOP_LEFT) && tile.check(Connections.TOP_RIGHT) && tile.check(Connections.BOTTOM_LEFT) && !tile.check(Connections.BOTTOM_RIGHT))
                                uv = new int[]{6, 4};
                            else {
                                if (!tile.check(Connections.TOP_LEFT) && tile.check(Connections.TOP_RIGHT) && !tile.check(Connections.BOTTOM_RIGHT) && tile.check(Connections.BOTTOM_LEFT))
                                    uv = new int[]{0, 4};
                                else if (tile.check(Connections.TOP_LEFT) && !tile.check(Connections.TOP_RIGHT) && tile.check(Connections.BOTTOM_RIGHT) && !tile.check(Connections.BOTTOM_LEFT))
                                    uv = new int[]{0, 5};
                                else if (!tile.check(Connections.TOP_LEFT) && !tile.check(Connections.TOP_RIGHT) && tile.check(Connections.BOTTOM_RIGHT) && tile.check(Connections.BOTTOM_LEFT))
                                    uv = new int[]{3, 4};
                                else if (tile.check(Connections.TOP_LEFT) && !tile.check(Connections.TOP_RIGHT) && !tile.check(Connections.BOTTOM_RIGHT) && tile.check(Connections.BOTTOM_LEFT))
                                    uv = new int[]{3, 5};
                                else if (tile.check(Connections.TOP_LEFT) && tile.check(Connections.TOP_RIGHT) && !tile.check(Connections.BOTTOM_RIGHT) && !tile.check(Connections.BOTTOM_LEFT))
                                    uv = new int[]{2, 5};
                                else if (!tile.check(Connections.TOP_LEFT) && tile.check(Connections.TOP_RIGHT) && tile.check(Connections.BOTTOM_RIGHT) && !tile.check(Connections.BOTTOM_LEFT))
                                    uv = new int[]{2, 4};
                                else {
                                    if (tile.check(Connections.TOP_LEFT))
                                        uv = new int[]{5, 5};
                                    else if (tile.check(Connections.TOP_RIGHT))
                                        uv = new int[]{4, 5};
                                    else if (tile.check(Connections.BOTTOM_RIGHT))
                                        uv = new int[]{4, 4};
                                    else if (tile.check(Connections.BOTTOM_LEFT))
                                        uv = new int[]{5, 4};
                                    else
                                        uv = new int[]{1, 4};
                                }
                            }
                        }
                    }
                }
            }
        }

        return uv;
    }

    private static EmiStack getFromGrid(int y, int x) {
        EmiStack[][] grid = StackManager.stackGrid;
        if (grid == null || y < 0 || y >= grid.length || grid[y] == null || x < 0 || x >= grid[y].length) return null;
        return grid[y][x];
    }

    private static StackGroup getGroup(EmiStack emiStack, SidebarType type) {
        if (emiStack == null) return null;
        if (emiStack instanceof EmiGroupStack gs) {
            if (StackManager.isGroupExpanded(type, gs.group.getId())) return gs.group;
        }
        if (emiStack instanceof GroupedEmiStack<?> ges) {
            if (StackManager.isGroupExpanded(type, ges.stackGroup.getId())) return ges.stackGroup;
        }
        var groupedStacks = StackGroupManager.stackToGroupedStacks.get(emiStack);
        if (groupedStacks != null) {
            for (var gs : groupedStacks) {
                if (StackManager.isGroupExpanded(type, gs.stackGroup.getId())) return gs.stackGroup;
            }
        }
        var idVariants = StackGroupManager.getItemToGroupedStacks().get(emiStack.getId());
        if (idVariants != null) {
            for (var gs : idVariants) {
                if (gs.realStack.isEqual(emiStack, Comparison.compareComponents())) {
                    if (StackManager.isGroupExpanded(type, gs.stackGroup.getId())) return gs.stackGroup;
                }
            }
        }
        return null;
    }

    private static StackGroup groupAt(int y, int x, SidebarType type) {
        return getGroup(getFromGrid(y, x), type);
    }

    public enum Connections {
        LEFT(1), TOP(2), RIGHT(4), BOTTOM(8),
        TOP_LEFT(16), TOP_RIGHT(32), BOTTOM_LEFT(64), BOTTOM_RIGHT(128);
        public final int bit;

        Connections(int bit) {
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

        public boolean check(Connections bit) {
            return (type & bit.bit) == bit.bit;
        }
    }
}