package com.evandev.emixx.mixin;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.emixx.feature.tag.TagCategoryManager;
import dev.emi.emi.EmiPort;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VanillaPlugin.class, remap = false)
public class VanillaPluginMixin {

    @Inject(method = "register", at = @At("HEAD"))
    private void onRegister(EmiRegistry registry, CallbackInfo ci) {
        registry.addCategory(TagCategoryManager.ITEM_TAGS);
        registry.addCategory(TagCategoryManager.BLOCK_TAGS);
        registry.addCategory(TagCategoryManager.FLUID_TAGS);
        registry.addCategory(TagCategoryManager.ENTITY_TYPE_TAGS);
    }

    @Inject(method = "initialize", at = @At("TAIL"))
    private void onInitialize(EmiInitRegistry registry, CallbackInfo ci) {
        registry.addRegistryAdapter(EmiRegistryAdapter.simple(Block.class, EmiPort.getBlockRegistry(), EmiStack::of));
        if (EmiPlusPlusConfig.enableEntityTags) {
            @SuppressWarnings("unchecked")
            Class<EntityType<?>> entityTypeClass = (Class) EntityType.class;
            registry.addRegistryAdapter(EmiRegistryAdapter.simple(entityTypeClass, BuiltInRegistries.ENTITY_TYPE, (entityType, changes, amount) -> {
                SpawnEggItem egg = SpawnEggItem.byId(entityType);
                if (egg != null) {
                    return EmiStack.of(egg, amount);
                }
                return EmiStack.EMPTY;
            }));
        }
    }
}
