package com.evandev.remi.feature.creativemodetab.gui.itemtab;

import com.evandev.ReliableEmi;
import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.integration.emi.ScreenManager;
import com.evandev.remi.util.GuiGraphicsUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class ItemTabButton extends TabButton {
    private static final ResourceLocation TEXTURE_DEFAULT = ReliableEmi.res("textures/gui/buttons.png");
    private static final ResourceLocation TEXTURE_LEFT = ReliableEmi.res("textures/gui/tab_button.png");
    private static final ResourceLocation TEXTURE_RIGHT = ReliableEmi.res("textures/gui/tab_button_right.png");

    private static Method recreativeIconMethod = null;
    private static boolean checkedRecreativeMethod = false;

    private final ItemTabManager tabManager;
    private final ItemTab tab;
    private final ButtonStyle style;
    private final boolean isFirst;
    private final Component title;
    private ResourceLocation customIcon;
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
        if (!checkedRecreativeMethod) {
            try {
                recreativeIconMethod = CreativeModeTab.class.getMethod("recreative$getCustomIcon");
            } catch (Exception ignored) {
            }
            checkedRecreativeMethod = true;
        }
        if (recreativeIconMethod != null) {
            try {
                return (ResourceLocation) recreativeIconMethod.invoke(tab);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private ResourceLocation getCustomIcon() {
        if (this.customIcon == null && this.tab.creativeModeTab() != null) {
            this.customIcon = fetchRecreativeIcon(this.tab.creativeModeTab());
        }
        return this.customIcon;
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
            ResourceLocation icon = getCustomIcon();

            if (style == ButtonStyle.TOP) {
                float yOff;
                if (isSelected()) {
                    context.drawTexture(TEXTURE_DEFAULT, getX(), getY(), 32, isHoveredOrFocused() ? 50 : 32, getWidth(), 18);
                    yOff = 4F;
                } else {
                    context.drawTexture(TEXTURE_DEFAULT, getX(), getY() + 2, 32, isHoveredOrFocused() ? 16 : 0, getWidth(), 16);
                    yOff = 5F;
                }

                if (icon != null) {
                    raw.pose().pushPose();
                    raw.pose().translate(getX() + 4.0, getY() + yOff, 150.0);
                    raw.pose().scale(10f / 16f, 10f / 16f, 1f);
                    raw.blit(icon, 0, 0, 0f, 0f, 16, 16, 16, 16);
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
                if (icon != null) {
                    raw.pose().pushPose();
                    raw.pose().translate(iconX, getY() + 5.0, 150.0);
                    raw.blit(icon, 0, 0, 0f, 0f, 16, 16, 16, 16);
                    raw.pose().popPose();
                } else if (tab.creativeModeTab() != null) {
                    GuiGraphicsUtils.renderItem(raw, tab.creativeModeTab().getIconItem(), iconX, getY() + 5F, 16F);
                }
            }

            if (isHovered && title != null) {
                if (ReliableEmiConfig.showCreativeTabNameInSearchbar && !ReliableEmiConfig.showTitleInsteadOfPageNumbers) {
                    ScreenManager.setCustomIndexTitle(title);
                    lastDisplayTitle = ScreenManager.customIndexTitle;
                }

                if (Minecraft.getInstance().screen != null) {
                    Minecraft.getInstance().screen.setTooltipForNextRenderPass(title);
                }
            } else if (!ReliableEmiConfig.showTitleInsteadOfPageNumbers) {
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