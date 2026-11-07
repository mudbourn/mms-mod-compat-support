package info.mudbourn.mmscompat.mixin.bubblellaneous;

import info.mudbourn.mmscompat.bubble.BubbleSuppressed;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.TreeMap;

/** Hides the Bubblellaneous functions and advancements that the Java port replaces. */
@Mixin(FileToIdConverter.class)
public abstract class BubbleResourceFilterMixin {

    @Shadow
    @Final
    private String prefix;

    @Inject(method = "listMatchingResources", at = @At("RETURN"), cancellable = true)
    private void mms$hideReplaced(ResourceManager manager, CallbackInfoReturnable<Map<Identifier, Resource>> cir) {
        if (!"function".equals(prefix) && !"advancement".equals(prefix)) {
            return;
        }
        Map<Identifier, Resource> files = cir.getReturnValue();
        if (files.keySet().stream().noneMatch(BubbleSuppressed::contains)) {
            return;
        }
        Map<Identifier, Resource> kept = new TreeMap<>(files);
        kept.keySet().removeIf(BubbleSuppressed::contains);
        cir.setReturnValue(kept);
    }
}
