package com.evandev.remi.mixin.emi;

import dev.emi.emi.network.CreateItemC2SPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreateItemC2SPacket.class, remap = false)
public abstract class CreateItemC2SPacketMixin {

    @Shadow
    @Final
    private ItemStack stack;

    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void sanitizeAndCheckPermissions(Player player, CallbackInfo ci) {
        if (player == null) {
            ci.cancel();
            return;
        }
        if (!player.hasPermissions(2) && !player.isCreative()) {
            ci.cancel();
            return;
        }
        if (stack != null && !stack.isEmpty()) {
            int maxCount = stack.getMaxStackSize();
            if (stack.getCount() > maxCount) {
                stack.setCount(maxCount);
            } else if (stack.getCount() <= 0) {
                stack.setCount(1);
            }
        }
    }
}
