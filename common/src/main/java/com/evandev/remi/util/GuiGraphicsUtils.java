package com.evandev.remi.util;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GuiGraphicsUtils {

    public static void renderItem(GuiGraphics guiGraphics, ItemStack stack, float x, float y, float size) {
        if (stack == null || stack.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        var bakedModel = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + size / 2F, y + size / 2F, 150F);
        try {
            guiGraphics.pose().scale(size, -size, size);
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
                    .setDetail("Item Components", stack.getComponents()::toString)
                    .setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
            throw new ReportedException(report);
        }
        guiGraphics.pose().popPose();
    }
}
