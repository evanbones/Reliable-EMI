package com.evandev.remi.mixin.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.stack.serializer.ItemEmiStackSerializer;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemEmiStackSerializer.class, remap = false)
public abstract class ItemEmiStackSerializerMixin {

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void remi$safeCreate(ResourceLocation id, DataComponentPatch componentChanges, long amount, CallbackInfoReturnable<EmiStack> cir) {
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            cir.setReturnValue(EmiStack.EMPTY);
        }
    }
}
