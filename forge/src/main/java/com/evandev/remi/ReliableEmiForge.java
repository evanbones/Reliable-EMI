package com.evandev.remi;

import com.evandev.ReliableEmi;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ReliableEmi.MOD_ID)
public class ReliableEmiForge {
    public ReliableEmiForge() {
        if (FMLEnvironment.dist.isClient()) {
            ReliableEmiClientForge.register();
        }
    }
}
