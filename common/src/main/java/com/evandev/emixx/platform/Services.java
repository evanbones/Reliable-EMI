package com.evandev.emixx.platform;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = ServiceLoader.load(IPlatformHelper.class)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No IPlatformHelper service found"));
}
