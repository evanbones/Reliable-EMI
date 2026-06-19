package com.evandev.emixx;

import com.evandev.EmiPlusPlus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(EmiPlusPlus.MOD_ID)
public class EmiPlusPlusNeoForge {
    public EmiPlusPlusNeoForge(IEventBus eventBus, ModContainer container) {
        if (FMLEnvironment.dist.isClient()) {
            EmiPlusPlusClientNeoForge.register(container);
        }
    }
}
