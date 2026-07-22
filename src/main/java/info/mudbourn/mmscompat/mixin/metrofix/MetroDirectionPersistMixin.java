package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.modmetro.MetroCartEntity;

/**
 * Persists MetroCartEntity#lastDirection across chunk reloads.
 *
 * ModMetro saves the wait state (IsWaiting, StationWaitTimer, stations)
 * to NBT but NOT lastDirection, which re-initializes to hardcoded east
 * (1,0,0). A train reloaded while waiting at a terminal then departs
 * along the reversed DEFAULT — west — instead of back down its line:
 * wrong axis or wrong way, the train jams at the terminal and the whole
 * line clogs behind it. Observed on MMSLive01 2026-07-22 after a nether
 * round-trip unloaded a terminal's chunks mid-wait.
 *
 * Zero direction is never written (freshly spawned carts), so load
 * falls back to the mod's default in that case.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroDirectionPersistMixin {

    @Shadow
    private Vec3 lastDirection;

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void mmsCompat$saveDirection(ValueOutput view, CallbackInfo ci) {
        Vec3 d = this.lastDirection;
        if (d != null && d.lengthSqr() > 1.0E-6) {
            view.putDouble("mmsCompat$LastDirX", d.x);
            view.putDouble("mmsCompat$LastDirY", d.y);
            view.putDouble("mmsCompat$LastDirZ", d.z);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void mmsCompat$loadDirection(ValueInput view, CallbackInfo ci) {
        double x = view.getDoubleOr("mmsCompat$LastDirX", 0.0);
        double y = view.getDoubleOr("mmsCompat$LastDirY", 0.0);
        double z = view.getDoubleOr("mmsCompat$LastDirZ", 0.0);
        Vec3 d = new Vec3(x, y, z);
        if (d.lengthSqr() > 1.0E-6) {
            this.lastDirection = d.normalize();
        }
    }
}
