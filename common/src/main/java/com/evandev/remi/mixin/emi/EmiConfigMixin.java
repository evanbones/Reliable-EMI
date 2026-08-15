package com.evandev.remi.mixin.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import dev.emi.emi.config.EmiConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(value = EmiConfig.class, remap = false)
public class EmiConfigMixin {

    @Inject(method = "loadConfig()V", at = @At(value = "INVOKE", target = "Ldev/emi/emi/config/EmiConfig;getConfigFile()Ljava/io/File;"))
    private static void remi$preventGlobalConfigOnLoad(CallbackInfo ci) {
        if (!ReliableEmiConfig.isLoaded()) {
            ReliableEmiConfig.load();
        }
        if (ReliableEmiConfig.disableEmiGlobalConfig) {
            EmiConfig.useGlobalConfig = false;
        }
    }

    @Inject(method = "getConfigFile", at = @At("HEAD"))
    private static void remi$preventGlobalConfigOnGetConfigFile(CallbackInfoReturnable<File> cir) {
        if (!ReliableEmiConfig.isLoaded()) {
            ReliableEmiConfig.load();
        }
        if (ReliableEmiConfig.disableEmiGlobalConfig) {
            EmiConfig.useGlobalConfig = false;
        }
    }
}
