package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import info.mudbourn.mmscompat.metro.MetroRailPath;
import info.mudbourn.mmscompat.metro.MetroTuning;

import com.example.modmetro.MetroCartEntity;

/**
 * Lets {@code lastDirection} follow a genuine reversal of travel. Fixes
 * detector-rail / terminal-loop layouts sending trains back out the way they
 * came in.
 *
 * ModMetro updates its cached heading in {@code tick} as:
 *
 * <pre>if (vel.lengthSqr() &gt; 0.01 &amp;&amp; lastDirection.dot(newDir) &gt;= 0.0) lastDirection = newDir;</pre>
 *
 * The {@code >= 0} guard makes the heading a ONE-WAY LATCH: a cart can never
 * record that it is now travelling the other way. That is harmless on a line
 * that only ever runs straight through, and wrong everywhere else — and the
 * mod reads no detector rails at all, so a Spanish-solution terminal built
 * out of redstone-switched track is exactly the case it cannot represent.
 *
 * The train arrives heading (say) east; the turnback curve sends it west;
 * the guard rejects that, so {@code lastDirection} stays east. Then the first
 * thing that consults it shoves the train back east:
 * {@code applyProximityBraking} restarts a stopped cart with
 * {@code lastDirection * 0.1}, and the station-wait release departs along
 * {@code lastDirection * 0.4}. The train turns around and reverses back into
 * the direction arrivals come from.
 *
 * The guard is not pointless — it rejects the single-tick direction noise the
 * follower correction impulses inject, and dropping it outright makes carts
 * jitter their heading. So instead of removing it, this redirect adds
 * hysteresis: motion must genuinely oppose the cached heading for
 * {@link #FLIP_TICKS} consecutive ticks, while the cart is on rail, before
 * the flip is accepted. Real reversals last; noise does not.
 *
 * Implemented as a redirect on the {@code dot} call itself — returning a
 * positive value is what admits the update — because that leaves the mod's
 * surrounding logic, including the speed gate, exactly as written.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroHeadingFlipMixin {

    /**
     * Distance actually travelled against the cached heading, in blocks, since
     * the last tick that agreed with it. Reset by any agreeing tick or by
     * leaving the rail.
     *
     * Deliberately a DISTANCE and not a tick count. The original version
     * counted 5 opposing ticks, sized for the 3.4 b/t line speed where that
     * meant ~17 blocks. Cruise zones cut carts to 0.8 b/t, which made the same
     * 5 ticks 4 blocks — and under braking a fraction of one — so a transient
     * rail bounce at a junction was enough to latch a permanent reversal and
     * send the train back the way it came. Blocks mean the same thing at every
     * speed; ticks do not.
     */
    @Unique
    private double mmsCompat$opposingDistance;

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;dot(Lnet/minecraft/world/phys/Vec3;)D"))
    private double mmsCompat$allowHeadingFlip(Vec3 lastDirection, Vec3 newDirection) {
        double dot = lastDirection.dot(newDirection);
        if (dot >= 0.0) {
            this.mmsCompat$opposingDistance = 0.0;
            return dot;
        }

        MetroCartEntity self = (MetroCartEntity) (Object) this;
        Level world = self.level();
        BlockPos pos = self.blockPosition();
        boolean onRail = MetroRailPath.isRail(world, pos) || MetroRailPath.isRail(world, pos.below());
        if (!onRail) {
            // Off-rail motion is not evidence of anything; the cart is being
            // repositioned, not driven.
            this.mmsCompat$opposingDistance = 0.0;
            return dot;
        }

        this.mmsCompat$opposingDistance += self.getDeltaMovement().horizontalDistance();
        if (this.mmsCompat$opposingDistance >= MetroTuning.heading_flip_distance) {
            if (MetroTuning.heading_flip_debug) {
                MetroTuning.log().info(
                        "[mms_compat] heading flip accepted for cart #{} at {} after {} blocks "
                        + "against heading {} -> {}",
                        self.getTrainIndex(), pos, String.format("%.2f", this.mmsCompat$opposingDistance),
                        lastDirection, newDirection);
            }
            this.mmsCompat$opposingDistance = 0.0;
            return 1.0; // sustained reversal over real distance: admit it
        }
        return dot;
    }
}
