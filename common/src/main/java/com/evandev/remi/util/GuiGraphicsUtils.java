package com.evandev.remi.util;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.Map;

public class GuiGraphicsUtils {

    private static final Map<String, NineSlice> SPRITES = Map.ofEntries(
            Map.entry("widget/text_field", NineSlice.of(200, 20, 8, 8, 8, 8)),
            Map.entry("widget/text_field_highlighted", NineSlice.of(200, 20, 8, 8, 8, 8)),
            Map.entry("widget/scrollbar_track", NineSlice.of(16, 7)),
            Map.entry("widget/scrollbar_track_vanilla", NineSlice.of(16, 6)),
            Map.entry("widget/scrollbar_background_vanilla", NineSlice.of(16, 7)),
            Map.entry("widget/scrollbar_thumb", NineSlice.of(16, 16, 7, 2, 7, 2)),
            Map.entry("widget/scrollbar_thumb_vanilla", NineSlice.of(16, 16, 6, 2, 6, 2))
    );
    private static final NineSlice TAB_SLICE = NineSlice.of(32, 9);

    private static NineSlice sliceFor(ResourceLocation sprite) {
        NineSlice slice = SPRITES.get(sprite.getPath());
        return slice != null ? slice : TAB_SLICE;
    }

    private static ResourceLocation textureFor(ResourceLocation sprite) {
        return new ResourceLocation(sprite.getNamespace(), "textures/gui/sprites/" + sprite.getPath() + ".png");
    }

    public static void blitSprite(GuiGraphics guiGraphics, ResourceLocation sprite, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        NineSlice slice = sliceFor(sprite);
        blitNineSliced(guiGraphics, textureFor(sprite), x, y, width, height,
                slice.left(), slice.top(), slice.right(), slice.bottom(),
                slice.textureWidth(), slice.textureHeight());
    }

    public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width,
                                      int height, int left, int top, int right, int bottom,
                                      int textureWidth, int textureHeight) {
        left = Math.min(left, width / 2);
        right = Math.min(right, width / 2);
        top = Math.min(top, height / 2);
        bottom = Math.min(bottom, height / 2);

        int innerWidth = width - left - right;
        int innerHeight = height - top - bottom;
        int uInner = textureWidth - left - right;
        int vInner = textureHeight - top - bottom;
        int uRight = textureWidth - right;
        int vBottom = textureHeight - bottom;

        blitTiled(guiGraphics, texture, x, y, left, top, 0, 0, left, top, textureWidth, textureHeight);
        blitTiled(guiGraphics, texture, x + left, y, innerWidth, top, left, 0, uInner, top, textureWidth, textureHeight);
        blitTiled(guiGraphics, texture, x + left + innerWidth, y, right, top, uRight, 0, right, top, textureWidth, textureHeight);

        blitTiled(guiGraphics, texture, x, y + top, left, innerHeight, 0, top, left, vInner, textureWidth, textureHeight);
        blitTiled(guiGraphics, texture, x + left, y + top, innerWidth, innerHeight, left, top, uInner, vInner, textureWidth, textureHeight);
        blitTiled(guiGraphics, texture, x + left + innerWidth, y + top, right, innerHeight, uRight, top, right, vInner, textureWidth, textureHeight);

        blitTiled(guiGraphics, texture, x, y + top + innerHeight, left, bottom, 0, vBottom, left, bottom, textureWidth, textureHeight);
        blitTiled(guiGraphics, texture, x + left, y + top + innerHeight, innerWidth, bottom, left, vBottom, uInner, bottom, textureWidth, textureHeight);
        blitTiled(guiGraphics, texture, x + left + innerWidth, y + top + innerHeight, right, bottom, uRight, vBottom, right, bottom, textureWidth, textureHeight);
    }

    private static void blitTiled(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height,
                                  int u, int v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        if (width <= 0 || height <= 0 || uWidth <= 0 || vHeight <= 0) return;
        for (int dx = 0; dx < width; dx += uWidth) {
            int tileWidth = Math.min(uWidth, width - dx);
            for (int dy = 0; dy < height; dy += vHeight) {
                int tileHeight = Math.min(vHeight, height - dy);
                guiGraphics.blit(texture, x + dx, y + dy, tileWidth, tileHeight, u, v, tileWidth, tileHeight, textureWidth, textureHeight);
            }
        }
    }

    public static void renderItem(GuiGraphics guiGraphics, ItemStack stack, float x, float y, float size) {
        if (stack == null || stack.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        var bakedModel = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + size / 2F, y + size / 2F, 150F);
        try {
            guiGraphics.pose().mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
            guiGraphics.pose().scale(size, size, size);
            boolean flat = !bakedModel.usesBlockLight();
            if (flat) Lighting.setupForFlatItems();
            minecraft.getItemRenderer().render(
                    stack, ItemDisplayContext.GUI, false, guiGraphics.pose(),
                    guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedModel
            );
            guiGraphics.flush();
            if (flat) Lighting.setupFor3DItems();
        } catch (Throwable t) {
            var report = CrashReport.forThrowable(t, "Rendering item");
            report.addCategory("Item being rendered")
                    .setDetail("Item Type", stack.getItem()::toString)
                    .setDetail("Item NBT", () -> String.valueOf(stack.getTag()))
                    .setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
            throw new ReportedException(report);
        }
        guiGraphics.pose().popPose();
    }

    private record NineSlice(int textureWidth, int textureHeight, int left, int top, int right, int bottom) {
        static NineSlice of(int size, int border) {
            return new NineSlice(size, size, border, border, border, border);
        }

        static NineSlice of(int width, int height, int left, int top, int right, int bottom) {
            return new NineSlice(width, height, left, top, right, bottom);
        }
    }
}
