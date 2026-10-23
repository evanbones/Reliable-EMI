package com.evandev.remi.platform;

import java.nio.file.Path;

public class Platform {
    public static Path getConfigDirectory() {
        //? if fabric {
        /*return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
        *///?} else if forge {
        /*return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
        *///?} else {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        //?}
    }
}
