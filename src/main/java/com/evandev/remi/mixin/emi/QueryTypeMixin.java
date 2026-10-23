package com.evandev.remi.mixin.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import dev.emi.emi.search.QueryType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = QueryType.class, remap = false)
public class QueryTypeMixin {

    @Inject(method = "fromString", at = @At("HEAD"), cancellable = true)
    private static void onFromString(String s, CallbackInfoReturnable<QueryType> cir) {
        for (int i = QueryType.values().length - 1; i >= 0; i--) {
            QueryType type = QueryType.values()[i];
            if (s.startsWith(type.prefix)) {
                if (type == QueryType.MOD && !ReliableEmiConfig.searchModPrefix) {
                    continue;
                }
                if (type == QueryType.TAG && !ReliableEmiConfig.searchTagPrefix) {
                    continue;
                }
                if (type == QueryType.TOOLTIP && !ReliableEmiConfig.searchTooltipPrefix) {
                    continue;
                }
                cir.setReturnValue(type);
                return;
            }
        }
        cir.setReturnValue(QueryType.DEFAULT);
    }
}
