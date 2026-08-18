package com.evandev.remi.gui.components;

import com.evandev.ReliableEmi;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.BooleanSupplier;

public class ImageButton extends SizedButtonWidget {
    private static final ResourceLocation DEFAULT_TEXTURE = ReliableEmi.res("textures/gui/buttons.png");

    private final BooleanSupplier isActiveSupplier;
    private final int baseU;
    private final int baseV;
    private boolean matchScreenManagerVisibility = false;
    private int textureWidth = 256;
    private int textureHeight = 256;

    public ImageButton(int width, int height, int u, int v, BooleanSupplier isActive, OnPress action) {
        super(0, 0, width, height, u, v, isActive, action);
        this.isActiveSupplier = isActive;
        this.baseU = u;
        this.baseV = v;
        this.texture = DEFAULT_TEXTURE;
    }

    public void matchScreenManagerVisibility() {
        this.matchScreenManagerVisibility = true;
    }

    public void withTexture(ResourceLocation customTexture, int width, int height) {
        this.texture = customTexture;
        this.textureWidth = width;
        this.textureHeight = height;
    }

    @Override
    public void renderWidget(GuiGraphics raw, int mouseX, int mouseY, float delta) {
        if (matchScreenManagerVisibility && EmiScreenManager.isDisabled()) return;

        this.active = isActiveSupplier.getAsBoolean();
        int currentV = baseV + (!this.active ? height * 2 : isMouseOver(mouseX, mouseY) ? height : 0);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        raw.blit(texture, getX(), getY(), baseU, currentV, width, height, textureWidth, textureHeight);

        if (isMouseOver(mouseX, mouseY) && text != null && active) {
            EmiDrawContext context = EmiDrawContext.wrap(raw);
            context.push();
            raw.pose().translate(0f, 0f, 400f);
            List<ClientTooltipComponent> texts = text.get().stream()
                    .map(EmiPort::ordered).map(ClientTooltipComponent::create).toList();
            EmiRenderHelper.drawTooltip(Minecraft.getInstance().screen, context, texts, mouseX, mouseY);
            context.pop();
        }
        RenderSystem.disableBlend();
    }
}
