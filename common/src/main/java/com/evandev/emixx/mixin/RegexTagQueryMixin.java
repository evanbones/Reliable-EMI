package com.evandev.emixx.mixin;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.search.RegexTagQuery;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Mixin(value = RegexTagQuery.class, remap = false)
public class RegexTagQueryMixin {

    @Shadow
    @Final
    private Set<Object> valid;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(String name, CallbackInfo ci) {
        if (!EmiPlusPlusConfig.enableTagSearchEnhancements) {
            return;
        }
        Pattern p = null;
        try {
            p = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        } catch (Exception ignored) {}
        if (p == null) return;
        final Pattern pat = p;

        List<Registry<?>> registries = List.of(
                BuiltInRegistries.ITEM,
                BuiltInRegistries.BLOCK,
                BuiltInRegistries.FLUID,
                BuiltInRegistries.ENTITY_TYPE
        );

        for (Registry<?> registry : registries) {
            registry.getTagNames().filter(tagKey -> {
                return pat.matcher(tagKey.location().toString()).find();
            }).forEach(tagKey -> {
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
