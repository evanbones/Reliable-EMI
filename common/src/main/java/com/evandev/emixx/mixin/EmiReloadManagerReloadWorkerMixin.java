package com.evandev.emixx.mixin;

import com.evandev.emixx.integration.emi.StackManager;
import com.evandev.emixx.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.emixx.feature.stackgroup.StackGroupManager;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.emi.emi.runtime.EmiReloadManager$ReloadWorker", remap = false)
public class EmiReloadManagerReloadWorkerMixin {

    @Inject(method = "run",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/registry/EmiStackList;bake()V", shift = At.Shift.AFTER))
    public void run(CallbackInfo ci) {
        EmiLog.LOG.info("[EMI++] Starting EMI++ reload...");
        var step = Component.literal("Baking stack groups");
        EmiLog.LOG.info("[EMI++] {}", step.getString());
        EmiReloadManager.reloadStep = step;
        EmiReloadManager.reloadWorry = System.currentTimeMillis() + 10_000;

        StackGroupManager.reload();
        StackManager.reload();
        CreativeModeTabManager.reload();
    }
}
