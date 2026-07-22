package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.example.modmetro.MetroCartEntity;

/**
 * Disables ModMetro's station-occupancy gate entirely.
 *
 * ModMetro separates trains two ways: proximity braking (position-based —
 * carts brake behind a train ahead, which actually keeps them apart) and
 * this gate (name-based — refuse to depart while any train on the line is
 * *registered* at the next station). The registration only clears once a
 * train is >10 blocks past the platform, so on a loaded line the gate is
 * true almost continuously and every departure just burns the timeout
 * (v0.4.1 bounded it at 15s, v0.5.2 at 5s) for zero separation benefit —
 * proximity braking already queues a second train safely short of an
 * occupied platform.
 *
 * History: v0.4.1 added a bounded-wait timeout because the unbounded gate
 * deadlocked cyclic lines permanently. v0.5.3 removes the gate outright.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroStationOccupancyDisableMixin {

    @Inject(method = "isNextStationOccupied", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$disableOccupancyGate(Level world, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
