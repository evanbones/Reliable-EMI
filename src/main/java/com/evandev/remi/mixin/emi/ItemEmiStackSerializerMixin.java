package com.evandev.remi.mixin.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.stack.serializer.ItemEmiStackSerializer;
//? if >=1.21 {
import net.minecraft.core.component.DataComponentPatch;
//?} else
//import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemEmiStackSerializer.class, remap = false)
public abstract class ItemEmiStackSerializerMixin {

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    //? if >=1.21 {
    private void remi$safeCreate(ResourceLocation id, DataComponentPatch componentChanges, long amount, CallbackInfoReturnable<EmiStack> cir) {
    //?} else
    //private void remi$safeCreate(ResourceLocation id, CompoundTag nbt, long amount, CallbackInfoReturnable<EmiStack> cir) {
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            cir.setReturnValue(EmiStack.EMPTY);
        }
    }
}
