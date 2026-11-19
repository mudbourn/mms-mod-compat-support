package info.mudbourn.mmscompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Iterates a snapshot of the dirty attribute set so mods can mark attributes dirty from
 * {@code onAttributeUpdated}.
 *
 * <p>Vanilla loops over {@code AttributeMap.getAttributesToUpdate()} and calls
 * {@code onAttributeUpdated} for each entry, which sets health and refreshes dimensions.
 * Mods that hook those (attribute-based size and health powers) can add to the same set
 * mid-loop, and the fastutil iterator then throws a NullPointerException ("Ticking player"
 * on join). The snapshot is taken and the set cleared before the loop; anything marked
 * dirty during it stays in the set and is handled on the next tick.
 */
@Mixin(LivingEntity.class)
public class DirtyAttributeSnapshotMixin {

    @WrapOperation(
        method = "refreshDirtyAttributes",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;")
    )
    private <T> Iterator<T> mmsCompat$snapshot(Set<T> dirty, Operation<Iterator<T>> original) {
        List<T> snapshot = new ArrayList<>(dirty);
        dirty.clear();
        return snapshot.iterator();
    }

    @WrapOperation(
        method = "refreshDirtyAttributes",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V")
    )
    private void mmsCompat$keepLateEntries(Set<?> dirty, Operation<Void> original) {
        // Already cleared before the loop; entries added during it wait for the next tick.
    }
}
