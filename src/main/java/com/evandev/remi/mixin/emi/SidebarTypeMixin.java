package com.evandev.remi.mixin.emi;

import com.evandev.remi.feature.workstation.WorkstationSidebarManager;
import dev.emi.emi.config.SidebarType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(value = SidebarType.class, remap = false)
public abstract class SidebarTypeMixin {

    @Shadow
    @Final
    @Mutable
    private static SidebarType[] $VALUES;

    @Invoker("<init>")
    public static SidebarType remi$createSidebarType(String enumName, int ordinal, String name, int u, int v) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void remi$addWorkstationType(CallbackInfo ci) {
        List<SidebarType> types = new ArrayList<>(Arrays.asList($VALUES));
        SidebarType workstation = remi$createSidebarType("WORKSTATION", types.size(), "workstation", 112, 146);
        types.add(workstation);
        $VALUES = types.toArray(new SidebarType[0]);
        WorkstationSidebarManager.WORKSTATION = workstation;
    }
}
