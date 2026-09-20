package com.evandev.remi.mixin.emi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.registry.EmiIngredientSerializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
                if (json.has("id")) {
                    String idStr = json.get("id").getAsString();
                    ResourceLocation id = ResourceLocation.tryParse(idStr);
                    if (id != null) {
                        if (BuiltInRegistries.MOB_EFFECT.containsKey(id)) {
                            json.addProperty("type", "mob_effect");
                        } else if (BuiltInRegistries.FLUID.containsKey(id)) {
                            json.addProperty("type", "fluid");
                        } else {
                            json.addProperty("type", "item");
                        }
                    } else {
                        json.addProperty("type", "item");
                    }
                } else if (json.has("tag")) {
                    json.addProperty("type", "tag");
                } else {
                    json.addProperty("type", "item");
                }
            }

            String type = json.get("type").getAsString();
            if (!EmiIngredientSerializers.BY_TYPE.containsKey(type)) {
                cir.setReturnValue(EmiStack.EMPTY);
            }
        }
    }

    @Inject(method = "serialize", at = @At("RETURN"))
    private static void remi$ensureSerializedType(EmiIngredient ingredient, CallbackInfoReturnable<JsonElement> cir) {
        JsonElement result = cir.getReturnValue();
        if (result != null && result.isJsonObject()) {
            JsonObject json = result.getAsJsonObject();
            if (!json.has("type") && ingredient != null) {
                EmiIngredientSerializer<?> serializer = EmiIngredientSerializers.BY_CLASS.get(ingredient.getClass());
                if (serializer != null && serializer.getType() != null) {
                    json.addProperty("type", serializer.getType());
                }
            }
        }
    }
}
