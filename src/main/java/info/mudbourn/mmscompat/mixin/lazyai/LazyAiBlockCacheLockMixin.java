package info.mudbourn.mmscompat.mixin.lazyai;

import info.mudbourn.mmscompat.lazyai.LockedReference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.pixelindiedev.lazy_ai_pixelindiedev.LazyAI$BlockChecker", remap = false)
public class LazyAiBlockCacheLockMixin {

    @Redirect(
        method = "<clinit>",
        at = @At(
            value = "NEW",
            target = "it/unimi/dsi/fastutil/objects/Reference2BooleanOpenHashMap"
        ),
        remap = false
    )
    private static Reference2BooleanOpenHashMap<Object> mms$lockedCache() {
        return new LockedReference2BooleanMap<>();
    }
}
