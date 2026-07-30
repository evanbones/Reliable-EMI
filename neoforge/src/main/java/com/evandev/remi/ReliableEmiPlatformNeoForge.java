package com.evandev.remi;

import com.evandev.remi.platform.IPlatformHelper;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ReliableEmiPlatformNeoForge implements IPlatformHelper {
    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
