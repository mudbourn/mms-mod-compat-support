package info.mudbourn.mmscompat.mixin.rei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Pseudo
@Mixin(targets = "me.shedaniel.rei.impl.common.entry.comparison.NbtHasherProviderImpl$Hasher", remap = false)
public class ReiNbtHasherNullFilterMixin {

    @WrapOperation(
        method = "hashIgnoringKeys",
        at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"),
        remap = false
    )
    private boolean mmsCompat$nullSafeFilter(Predicate<Object> filter, Object type, Operation<Boolean> original) {
        // filter == null is REI's "hash all components" variant — treat every key as included.
        return filter == null || original.call(filter, type);
    }
}
