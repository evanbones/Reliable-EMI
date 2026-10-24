package com.evandev.remi.mixin.controlify;

import com.evandev.remi.feature.creativemodetab.gui.CreativeModeTabGui;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.Set;

// Controlify snaps the virtual mouse to the centre of every widget on the screen, including our hidden
// creative tab widgets
@IfModLoaded("controlify")
@Mixin(value = VirtualMouseHandler.class, remap = false)
public class VirtualMouseHandlerMixin {
    @ModifyReturnValue(method = "collectSnapPoints", at = @At("RETURN"), require = 0)
    private Set<SnapPoint> remi$removeCreativeTabSnapPoints(Set<SnapPoint> points) {
        Screen screen = Minecraft.getInstance().screen;
        if (points.isEmpty() || screen == null) return points;

        Set<Vector2ic> centres = new HashSet<>();
        for (AbstractWidget widget : CreativeModeTabGui.getWidgets()) {
            if (screen.children().contains(widget)) {
                centres.add(new Vector2i(widget.getX() + widget.getWidth() / 2, widget.getY() + widget.getHeight() / 2));
            }
        }
        if (centres.isEmpty()) return points;

        Set<SnapPoint> filtered = new HashSet<>(points);
        filtered.removeIf(point -> centres.contains(point.position()));
        return filtered;
    }
}
