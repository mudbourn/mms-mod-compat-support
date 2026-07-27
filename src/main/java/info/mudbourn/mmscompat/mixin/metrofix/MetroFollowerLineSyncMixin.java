package info.mudbourn.mmscompat.mixin.metrofix;

import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.modmetro.MetroCartEntity;

/**
 * Propagates {@code lineName} from the consist lead onto followers.
 *
 * ModMetro's {@code tickFollowerCart} copies CURRENT_STATION and
 * NEXT_STATION from the lead cart, but only inside its "settled into
 * spacing" branch, and it never copies {@code lineName} at all —
 * {@code lineName} is set exclusively on whichever cart last touched a
 * station block. {@link info.mudbourn.mmscompat.metro.MetroLineSyncServer}
 * works around this by reading off the consist lead directly, but that only
 * fixes the value riders see; anything else that reads a follower's own
 * {@code lineName} still gets an empty string. This mixin fixes the field
 * itself, unconditionally, on every tick the lead is resolvable — including
 * the early-return paths ModMetro's own copy never reaches.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroFollowerLineSyncMixin {

    @Shadow
    private UUID leadCartUuid;

    @Inject(method = "tickFollowerCart", at = @At("RETURN"))
    private void mmsCompat$syncLine(Level world, CallbackInfo ci) {
        if (this.leadCartUuid == null || !(world instanceof ServerLevel sw)) {
            return;
        }
        if (!(sw.getEntity(this.leadCartUuid) instanceof MetroCartEntity lead)) {
            return;
        }
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        if (lead == self) {
            return;
        }
        String leadLine = ((MetroCartLineAccessor) lead).mmsCompat$getLineName();
        ((MetroCartStateAccessor) self).mmsCompat$setLine(leadLine == null ? "" : leadLine);
    }
}
