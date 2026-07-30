package com.evandev.remi;

import com.evandev.ReliableEmi;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(ReliableEmi.MOD_ID)
public class ReliableEmiNeoForge {
    public ReliableEmiNeoForge(IEventBus eventBus, ModContainer container) {
        if (FMLEnvironment.dist.isClient()) {
            ReliableEmiClientNeoForge.register(container);
        }
    }
}
