package com.evandev.remi.integration.controlify;

import dev.isxander.controlify.api.ControlifyApi;

public final class ControlifyCompat {
    private static final boolean LOADED = locate();

    public static boolean isControllerActive() {
        return LOADED && Api.isControllerActive();
    }

    private static boolean locate() {
        try {
            Class.forName("dev.isxander.controlify.api.ControlifyApi", false, ControlifyCompat.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static final class Api {
        static boolean isControllerActive() {
            return ControlifyApi.get().currentInputMode().isController();
        }
    }
}
