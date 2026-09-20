package com.evandev.remi.mixin.emi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.registry.EmiIngredientSerializers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EmiIngredientSerializers.class, remap = false)
public abstract class EmiIngredientSerializersMixin {

    @Inject(method = "deserialize", at = @At("HEAD"), cancellable = true)
    private static void remi$sanitizeDeserialize(JsonElement element, CallbackInfoReturnable<EmiIngredient> cir) {
        if (element == null || element.isJsonNull()) {
            cir.setReturnValue(EmiStack.EMPTY);
            return;
        }

        if (element.isJsonObject()) {
            JsonObject json = element.getAsJsonObject();
            if (!json.has("type")) {
                if (json.has("neoforge:cures") || json.has("ambient") || json.has("duration") || json.has("amplifier")) {
                    json.addProperty("type", "mob_effect");
                } else if (json.has("item")) {
                    json.addProperty("type", "item");
                } else if (json.has("fluid")) {
                    json.addProperty("type", "fluid");
                } else if (json.has("tag")) {
                    json.addProperty("type", "tag");
                } else {
                    cir.setReturnValue(EmiStack.EMPTY);
                    return;
                }
            }

            JsonElement typeEl = json.get("type");
            String type = typeEl != null && typeEl.isJsonPrimitive() ? typeEl.getAsString() : null;
            if (type == null || (!EmiIngredientSerializers.BY_TYPE.containsKey(type) && !type.startsWith("emi:"))) {
                cir.setReturnValue(EmiStack.EMPTY);
            }
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String s = element.getAsString();
            String type = s.split(":")[0];
            if (!type.startsWith("#") && !EmiIngredientSerializers.BY_TYPE.containsKey(type)) {
                cir.setReturnValue(EmiStack.EMPTY);
            }
        }
    }

    @Inject(method = "serialize", at = @At("RETURN"))
    private static void remi$ensureSerializedType(EmiIngredient ingredient, CallbackInfoReturnable<JsonElement> cir) {
        JsonElement result = cir.getReturnValue();
        if (result != null && result.isJsonObject() && ingredient != null) {
            JsonObject json = result.getAsJsonObject();
            if (!json.has("type")) {
                EmiIngredientSerializer<?> serializer = EmiIngredientSerializers.BY_CLASS.get(ingredient.getClass());
                if (serializer != null && serializer.getType() != null) {
                    json.addProperty("type", serializer.getType());
                }
            }
        }
    }
}
