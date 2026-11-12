package info.mudbourn.mmscompat.mixin.alexscaves;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

/**
 * Keeps the sherd-to-pattern map writable after Alex's Caves rebuilds it as an
 * ImmutableMap, so Fish of Thieves and other mods that add sherds later do not crash.
 */
@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.misc.ACPotPatternRegistry", remap = false)
public class AcPotPatternMutableMixin {

    @Inject(method = "expandVanillaDefinitions", at = @At("TAIL"), remap = false)
    private static void mms$unfreeze(CallbackInfo ci) {
        DecoratedPotPatternsAccessor.mms$setItemToPotTexture(new HashMap<>(DecoratedPotPatternsAccessor.mms$itemToPotTexture()));
    }
}
