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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lets a Better Combat <em>swing</em> own the legs while the player is standing
 * still, and hands them back to DetailedAnimations the moment the player is doing
 * anything that has its own footwork.
 *
 * <h2>Why legs need a producer at all</h2>
 *
 * <p>EMF Compat's Better Combat addon saves both arms under {@code better_combat}
 * for exactly the duration of a swing, and {@link HeldPoseMixin} covers the arms of
 * an idle hold under {@code mms_held_pose}. Neither ever saves legs, pants or body,
 * so DA is the unconditional last writer there and toheee1234's ~1600 leg keyframes
 * per animation never reach the screen. This fills only that gap.
 *
 * <h2>When it captures</h2>
 *
 * <p>Only while a swing is playing. {@code better_combat} is the addon's own key,
 * stored for exactly the duration of an attack and cleared otherwise, so its
 * presence is the honest probe for that and costs no extra dependency.
 *
 * <p>An idle hold deliberately does <em>not</em> qualify, and that is the fix this
 * gate encodes. Through 0.9.83 it also captured whenever {@link HeldPoseMixin} had
 * stored a hold, on the reasoning that a posed weapon is a posed weapon. But a hold
 * is an <em>arm</em> statement — where the weapon sits — and the pose animations
 * carry a full stance underneath it. Reading legs out of one froze the player's
 * stance the whole time a spear was carried, replacing DA's idle for no gain: the
 * weapon looks identical either way, because nothing about the hold is below the
 * waist. A swing is different; its footwork is the animation.
 *
 * <p>Priority still outranks {@link HeldPoseMixin} even though the read of its
 * store is gone. Both inject at {@code RETURN} of the same method, and a
 * later-applied mixin's callback lands after an earlier one's, so the higher number
 * runs second; that ordering costs nothing here and keeps the two producers'
 * relationship stable if this ever reads from that store again.
 *
 * <h2>When it yields</h2>
 *
 * <p>{@code walkAnimationSpeed} is the amplitude vanilla and DA both scale limb
 * swing by. It decays to zero when the player stops and while airborne, so one test
 * covers standing still, hovering and the tail of a landing. It gates <em>taking</em>
 * the legs: authority is never seized mid-stride, where DA's contribution is large
 * and the grab would be the pop.
 *
 * <p>It deliberately does not gate <em>keeping</em> them. Through 0.9.99 it did, and
 * the two ends of the same swing then ran on different clocks: the arms are held for
 * exactly the addon's swing, while the legs were re-decided every frame and dropped
 * the instant the player drifted off the spot. Nudging a movement key mid-attack put
 * the top half in the attack and the bottom half back in DA's walk — the visible
 * break being chased. Once taken, the legs are now held until the swing itself ends,
 * so both halves change authority on the same frame.
 *
 * <p>The cost of that is real and is the trade being made: releasing at the end of a
 * swing no longer waits for a moment when DA has nothing to say, so a swing that ends
 * while the player is moving hands the legs back to a non-zero walk cycle and pops.
 * The old gate bought that away by desynchronising the halves. Nothing here blends —
 * {@code PoseBlend} is arms-only, and off by default — so the pop is not smoothed;
 * it is simply preferred to the mismatch.
 *
 * <p>Two states still yield outright, because they do not move that value and their
 * conflict is with the pose itself rather than its timing: crouching is a held pose
 * rather than an idle, and water has its own leg cycle that runs whether or not the
 * player is making headway — treading water is stationary by that measure but is
 * emphatically not standing. Any contact with water yields, which also covers
 * standing in the shallows; DA's idle legs there are a far smaller artefact than a
 * dry-land stance played while submerged.
 */
@Mixin(value = PlayerModel.class, priority = 3000)
public class HeldPoseLegMixin {

    private static final String SOURCE = HeldPoseSource.LEG_SOURCE;

    /** The addon's own key, read only as a probe for "a swing is playing". */
    private static final String BETTER_COMBAT_SOURCE = "better_combat";

    /** Below this the limb animation counts as stopped and the pose takes over. */
    private static final float LIMBS_SETTLED = 0.01F;

    /**
     * Players whose legs this currently owns, so the settle test can gate taking
     * them without also gating keeping them.
     *
     * <p>An entry lives only as long as one swing: every path that stops capturing
     * removes it, and the common one — {@code better_combat} gone, i.e. not swinging —
     * runs on nearly every frame for nearly every player. Nothing accumulates for a
     * player who logs out mid-swing beyond the next frame they are not rendered.
     */
    private static final Set<UUID> HOLDING = ConcurrentHashMap.newKeySet();

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void mms$captureHeldPoseLegs(AvatarRenderState state, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(state.id);
        if (!(entity instanceof AbstractClientPlayer player)) {
            return;
        }

        UUID uuid = player.getUUID();

        // The addon stores arms under its key for exactly the duration of a swing
        // and clears them otherwise, so this is the cheapest honest probe for
        // "an attack animation is playing" and costs no extra dependency. The
        // second half is HeldPoseMixin's verdict for this frame; see the class
        // doc for why that is a read and not a re-derivation.
        boolean posed = PoseManager.getSavedPoses(uuid, BETTER_COMBAT_SOURCE) != null;

        // These conflict with the pose itself, not merely with the moment of
        // handover, so they end the capture whenever they turn up.
        boolean conflictingStance = player.isCrouching()
                || player.isInWater()
                || player.isVisuallySwimming();

        if (!posed || conflictingStance) {
            HOLDING.remove(uuid);
            PoseManager.clearPoses(uuid, SOURCE);
            return;
        }

        // The settle test applies only to acquiring. Once the legs are held they stay
        // held for the rest of the swing, so they change authority on the same frame
        // the arms do; see the class doc for what that trades away.
        if (!HOLDING.contains(uuid) && state.walkAnimationSpeed > LIMBS_SETTLED) {
            PoseManager.clearPoses(uuid, SOURCE);
            return;
        }
        HOLDING.add(uuid);

        PlayerModel model = (PlayerModel) (Object) this;

        Map<String, PoseSnapshot> parts = new HashMap<>();
        parts.put("left_leg", new PoseSnapshot(model.leftLeg));
        parts.put("right_leg", new PoseSnapshot(model.rightLeg));
        parts.put("left_pants", new PoseSnapshot(model.leftPants));
        parts.put("right_pants", new PoseSnapshot(model.rightPants));

        PoseManager.savePoses(uuid, SOURCE, null, null, parts);
    }
}
