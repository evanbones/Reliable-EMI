package com.evandev.emixx.feature.creativemodetab.gui.itemtab;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.EmiPlusPlus;
import com.evandev.emixx.integration.emi.ScreenManager;
import com.evandev.emixx.util.GuiGraphicsUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.NotNull;

public class ItemTabButton extends TabButton {
    private static final ResourceLocation TEXTURE_DEFAULT = EmiPlusPlus.res("textures/gui/buttons.png");
    private static final ResourceLocation TEXTURE_LEFT = EmiPlusPlus.res("textures/gui/tab_button.png");
    private static final ResourceLocation TEXTURE_RIGHT = EmiPlusPlus.res("textures/gui/tab_button_right.png");

    private final ItemTabManager tabManager;
    private final ItemTab tab;
    private final ButtonStyle style;
    private final boolean isFirst;
    private final Component title;
    private final ResourceLocation customIcon;
    private Component lastDisplayTitle;

    public ItemTabButton(ItemTabManager tabManager, ItemTab tab, int width, int height,
                         ButtonStyle style, boolean isFirst) {
        super(tabManager, tab, width, height);
        this.tabManager = tabManager;
        this.tab = tab;
        this.style = style;
        this.isFirst = isFirst;
        this.title = tab.creativeModeTab() != null ? tab.creativeModeTab().getDisplayName() : null;
        this.customIcon = fetchRecreativeIcon(tab.creativeModeTab());
    }

    private static ResourceLocation fetchRecreativeIcon(CreativeModeTab tab) {
        if (tab == null) return null;
        try {
            var method = tab.getClass().getMethod("recreative$getCustomIcon");
            return (ResourceLocation) method.invoke(tab);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isVisible() {
        return tab.creativeModeTab() != null;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!isVisible()) return;
        super.onClick(mouseX, mouseY);
        tabManager.onTabSelected(tab);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics raw, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        EmiDrawContext context = EmiDrawContext.wrap(raw);

        if (isVisible()) {
            if (style == ButtonStyle.TOP) {
                float yOff;
                if (isSelected()) {
                    context.drawTexture(TEXTURE_DEFAULT, getX(), getY(), 32, isHoveredOrFocused() ? 50 : 32, getWidth(), 18);
                    yOff = 4F;
                } else {
                    context.drawTexture(TEXTURE_DEFAULT, getX(), getY() + 2, 32, isHoveredOrFocused() ? 16 : 0, getWidth(), 16);
                    yOff = 5F;
                }

                if (this.customIcon != null) {
                    raw.pose().pushPose();
                    raw.pose().translate(getX() + 4.0, getY() + yOff, 150.0);
                    raw.pose().scale(10f / 16f, 10f / 16f, 1f);
                    raw.blit(this.customIcon, 0, 0, 0f, 0f, 16, 16, 16, 16);
                    raw.pose().popPose();
                } else if (tab.creativeModeTab() != null) {
                    GuiGraphicsUtils.renderItem(raw, tab.creativeModeTab().getIconItem(), getX() + 4F, getY() + yOff, 10F);
                }
            } else {
                int u = isSelected() ? 188 : 152;
                int v = (isSelected() && isFirst) ? 29 : 2;
                ResourceLocation texture = (style == ButtonStyle.RIGHT) ? TEXTURE_RIGHT : TEXTURE_LEFT;
                raw.pose().pushPose();
                raw.pose().translate(0.0, 0.0, isSelected() ? 100.0 : 0.0);
                context.drawTexture(texture, getX(), getY(), u, v, getWidth(), getHeight());
                raw.pose().popPose();

                float iconX = (style == ButtonStyle.RIGHT) ? getX() + 6F : getX() + 8F;
                if (this.customIcon != null) {
                    raw.pose().pushPose();
                    raw.pose().translate(iconX, getY() + 5.0, 150.0);
                    raw.blit(this.customIcon, 0, 0, 0f, 0f, 16, 16, 16, 16);
                    raw.pose().popPose();
                } else if (tab.creativeModeTab() != null) {
                    GuiGraphicsUtils.renderItem(raw, tab.creativeModeTab().getIconItem(), iconX, getY() + 5F, 16F);
                }
            }

            if (isHovered && title != null) {
                var font = Minecraft.getInstance().font;
                int spaceWidth = ScreenManager.indexScreenSpace != null ? ScreenManager.indexScreenSpace.tw : 0;
                int maxWidth = spaceWidth * ScreenManager.ENTRY_SIZE - 20;

                Component displayTitle = (maxWidth > 0 && font.width(title) > maxWidth)
                        ? Component.literal(font.plainSubstrByWidth(title.getString(), maxWidth - font.width("...")) + "...")
                        : title;

                if (EmiPlusPlusConfig.showCreativeTabNameInSearchbar) {
                    lastDisplayTitle = displayTitle;
                    ScreenManager.customIndexTitle = displayTitle;
                }
                raw.renderTooltip(font, title, mouseX, mouseY);
            } else {
                ScreenManager.removeCustomIndexTitle(lastDisplayTitle != null ? lastDisplayTitle : title);
            }
        } else if (style == ButtonStyle.TOP) {
            context.drawTexture(TEXTURE_DEFAULT, getX(), getY() + getHeight() - 2, 32, 14, getWidth(), 2);
            context.fill(getX(), getY() + 2, getWidth(), getHeight() - 4, 0xDB000000);
        }

        RenderSystem.disableBlend();
    }

    public enum ButtonStyle {TOP, LEFT, RIGHT}
}