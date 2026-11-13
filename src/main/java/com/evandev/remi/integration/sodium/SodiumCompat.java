package com.evandev.remi.integration.sodium;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class SodiumCompat {
    private static final String MODERN_SPRITE_UTIL = "net.caffeinemc.mods.sodium.api.texture.SpriteUtil";
    private static final String LEGACY_SPRITE_UTIL = "me.jellysquid.mods.sodium.client.render.texture.SpriteUtil";
    private static final MethodType MARK_SPRITE_ACTIVE_TYPE = MethodType.methodType(void.class, TextureAtlasSprite.class);
    private static final MethodHandle MARK_SPRITE_ACTIVE = findMarkSpriteActive();

    public static boolean isLoaded() {
        return MARK_SPRITE_ACTIVE != null;
    }

    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (MARK_SPRITE_ACTIVE == null || sprite == null) return;
        try {
            MARK_SPRITE_ACTIVE.invoke(sprite);
        } catch (Throwable ignored) {
        }
    }

    private static MethodHandle findMarkSpriteActive() {
        MethodHandle modern = findModernMarkSpriteActive();
        return modern != null ? modern : findLegacyMarkSpriteActive();
    }

    private static MethodHandle findModernMarkSpriteActive() {
        try {
            Class<?> spriteUtil = Class.forName(MODERN_SPRITE_UTIL);
            Object instance = spriteUtil.getField("INSTANCE").get(null);
            return MethodHandles.lookup()
                    .findVirtual(spriteUtil, "markSpriteActive", MARK_SPRITE_ACTIVE_TYPE)
                    .bindTo(instance);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static MethodHandle findLegacyMarkSpriteActive() {
        try {
            Class<?> spriteUtil = Class.forName(LEGACY_SPRITE_UTIL);
            return MethodHandles.lookup().findStatic(spriteUtil, "markSpriteActive", MARK_SPRITE_ACTIVE_TYPE);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
