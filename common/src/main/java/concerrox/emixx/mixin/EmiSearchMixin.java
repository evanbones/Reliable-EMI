package concerrox.emixx.mixin;

import com.google.common.collect.Sets;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.searchtree.SuffixArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiSearch.class, remap = false)
public class EmiSearchMixin {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void initSearchFields(CallbackInfo ci) {
        if (EmiSearch.names == null) {
            EmiSearch.names = new SuffixArray<>();
        }
        if (EmiSearch.tooltips == null) {
            EmiSearch.tooltips = new SuffixArray<>();
        }
        if (EmiSearch.mods == null) {
            EmiSearch.mods = new SuffixArray<>();
        }
        if (EmiSearch.aliases == null) {
            EmiSearch.aliases = new SuffixArray<>();
        }
        if (EmiSearch.bakedStacks == null) {
            EmiSearch.bakedStacks = Sets.newIdentityHashSet();
        }
    }
}
