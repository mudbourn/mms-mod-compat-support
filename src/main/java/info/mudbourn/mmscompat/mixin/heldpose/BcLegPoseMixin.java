package info.mudbourn.mmscompat.mixin.heldpose;

import info.mudbourn.mmscompat.client.HeldPoseSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import strm.emfcompat.core.PoseManager;
import strm.emfcompat.core.PoseSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps a Better Combat attack animation's legs from being replaced by
 * DetailedAnimations' idle legs.
 *
 * <h2>The gap</h2>
 *
 * <p>EMF Compat's Better Combat addon saves exactly two snapshots —
 * {@code left_arm} and {@code right_arm}. Legs, pants and body are never stored, so
 * DA's CEM pass is the unconditional last writer on the legs. Attack packs that
 * animate legs therefore lose them entirely: toheee1234's pack has ~1600 keyframes
 * on each leg and none of them survive to the screen.
 *
 * <p>Nothing here is a conflict to resolve — there has simply never been a producer
 * for the legs, the same way there was never one for idle held poses.
 *
 * <h2>When the attack legs win, and when DA's do</h2>
 *
 * <p>Only while standing still. A player who is walking or running should keep DA's
 * gait — an attack animation's legs replacing a run cycle reads far worse than the
 * attack losing its footwork — so while moving this stores nothing and DA is left
 * alone.
 *
 * <p>The threshold is deliberately Better Combat's own, read out of
 * {@code AttackAnimationStack}: sprinting, or horizontal velocity above
 * {@code 0.03}. Better Combat already fades its own leg animation out on exactly
 * that condition, so matching it means both switch on the same frame. Picking a
 * different number here — DA's {@code limb_speed > 0.1}, say — would open a window
 * where Better Combat has given up on the legs but this is still replaying them.
 *
 * <h2>Absolute, not additive</h2>
 *
 * <p>Unlike {@link HeldPoseAdditiveMixin}, this stores absolutes and lets EMF
 * Compat's own applier write them. That is correct here: the intent is that DA's
 * idle legs have <em>no</em> claim while an attack plays standing still, so
 * replacing them outright is the desired composition rather than a lossy one. It
 * also means no new seam and no ordering fight — the existing applier already
 * handles {@code left_leg}, {@code right_leg}, {@code left_pants} and
 * {@code right_pants} from the parts map.
 *
 * <h2>Ordering</h2>
 *
 * <p>Priority 500, below the addon's default 1000. Mixins are applied in ascending
 * priority and the first-applied injection at a shared point runs last, so this
 * runs <em>after</em> the addon's own capture — which is what makes the
 * {@code better_combat} store a reliable current-frame answer to "is an attack
 * playing", rather than a stale one from the previous frame.
 */
@Mixin(value = PlayerModel.class, priority = 500)
public class BcLegPoseMixin {

    private static final String SOURCE = HeldPoseSource.LEG_SOURCE;

    /** The Better Combat addon's own key, consulted read-only as an attack probe. */
    private static final String BETTER_COMBAT_SOURCE = "better_combat";

    /** {@code AttackAnimationStack.isWalking} — horizontal speed above this is moving. */
    private static final double WALKING_SPEED = 0.03;

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void mms$captureAttackLegs(AvatarRenderState state, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(state.id);
        if (!(entity instanceof AbstractClientPlayer player)) {
            return;
        }

        UUID uuid = player.getUUID();

        // Only while an attack is actually playing. The addon stores arms under its
        // own key for exactly the duration of a swing and clears them otherwise, so
        // this is the cheapest honest probe and costs no extra dependency.
        boolean attacking = PoseManager.getSavedPoses(uuid, BETTER_COMBAT_SOURCE) != null;
        if (!attacking || mms$isMoving(player)) {
            PoseManager.clearPoses(uuid, SOURCE);
            return;
        }

        PlayerModel model = (PlayerModel) (Object) this;

        Map<String, PoseSnapshot> parts = new HashMap<>();
        parts.put("left_leg", new PoseSnapshot(model.leftLeg));
        parts.put("right_leg", new PoseSnapshot(model.rightLeg));
        parts.put("left_pants", new PoseSnapshot(model.leftPants));
        parts.put("right_pants", new PoseSnapshot(model.rightPants));

        PoseManager.savePoses(uuid, SOURCE, null, null, parts);
    }

    private static boolean mms$isMoving(AbstractClientPlayer player) {
        return player.isSprinting()
                || player.getDeltaMovement().horizontalDistance() > WALKING_SPEED;
    }
}
