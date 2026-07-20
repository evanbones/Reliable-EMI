package com.evandev.emixx.integration.sodium;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Sodium skips animating sprites that were not marked active during the frame.
 * Stacks baked into EMI's batch VBOs bypass the item renderer Sodium hooks into,
 * so anything we batch ourselves has to mark its sprites explicitly each frame.
 */
public final class SodiumCompat {
    private static final MethodHandle MARK_SPRITE_ACTIVE = locate();

    private SodiumCompat() {
    }

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

    private static MethodHandle locate() {
        try {
            // Sodium 0.6+
            Class<?> clazz = Class.forName("net.caffeinemc.mods.sodium.api.texture.SpriteUtil");
            Object instance = clazz.getField("INSTANCE").get(null);
            return MethodHandles.lookup()
                    .findVirtual(clazz, "markSpriteActive", MethodType.methodType(void.class, TextureAtlasSprite.class))
                    .bindTo(instance);
        } catch (Throwable ignored) {
        }
        try {
            // Sodium 0.5
            Class<?> clazz = Class.forName("me.jellysquid.mods.sodium.client.render.texture.SpriteUtil");
            return MethodHandles.lookup()
                    .findStatic(clazz, "markSpriteActive", MethodType.methodType(void.class, TextureAtlasSprite.class));
        } catch (Throwable ignored) {
        }
        return null;
    }
}
