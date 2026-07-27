package info.mudbourn.mmscompat.mixin.metrofix;

import java.util.UUID;

import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.modmetro.MetroCartEntity;

/**
 * Fixes the husk-train-on-reboot bug: {@code load(ValueInput)} restores
 * {@code leadCartUuid} and {@code trainIndex} into their private fields only.
 * The synched {@code TRAIN_INDEX} entry is never touched on the load path —
 * it is written only by {@code setTrainData}, called from the spawner and
 * from {@link MetroReverseConsistMixin}. {@code getTrainIndex()} reads the
 * synched value, so after every restart every cart reports index 0 while its
 * field holds the true number: followers can't find their front cart
 * ({@code findFrontCart} matches on the synched value), and
 * {@code isNextStationOccupied} sees every cart in the world as index 0,
 * permanently occupying every station.
 *
 * Fix: at TAIL of {@code load}, once both fields are restored, push them into
 * the synched data via the already-public {@code setTrainData}.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroTrainIndexSyncMixin {

    @Shadow
    private UUID leadCartUuid;

    @Shadow
    private int trainIndex;

    @Inject(method = "load", at = @At("TAIL"))
    private void mmsCompat$resyncTrainIndex(ValueInput view, CallbackInfo ci) {
        if (this.leadCartUuid == null) {
            return;
        }
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        if (self.getTrainIndex() != this.trainIndex || !this.leadCartUuid.equals(self.getLeadCartUuid())) {
            self.setTrainData(this.leadCartUuid, this.trainIndex);
        }
    }
}
