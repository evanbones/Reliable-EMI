package com.evandev.remi.mixin.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.search.TagQuery;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Mixin(value = TagQuery.class, remap = false)
public class TagQueryMixin {

    @Shadow
    @Final
    private Set<Object> valid;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(String name, CallbackInfo ci) {
        if (!ReliableEmiConfig.enableTagSearchEnhancements) {
            return;
        }
        String lowerName = name.toLowerCase(Locale.ROOT);
        List<Registry<?>> registries = List.of(
                BuiltInRegistries.ITEM,
                BuiltInRegistries.BLOCK,
                BuiltInRegistries.FLUID,
                BuiltInRegistries.ENTITY_TYPE
        );

        for (Registry<?> registry : registries) {
            registry.getTagNames().filter(tagKey -> tagKey.location().toString().toLowerCase(Locale.ROOT).contains(lowerName)
                    || tagKey.location().getPath().toLowerCase(Locale.ROOT).contains(lowerName)).forEach(tagKey -> {
                TagEmiIngredient tagIngredient = new TagEmiIngredient(tagKey, 1);
                for (EmiStack stack : tagIngredient.getEmiStacks()) {
                    if (stack != null && !stack.isEmpty()) {
                        this.valid.add(stack.getKey());
                        if (stack.getItemStack() != null && !stack.getItemStack().isEmpty()) {
                            this.valid.add(stack.getItemStack().getItem());
                        }
                    }
                }
            });
        }
    }
}
