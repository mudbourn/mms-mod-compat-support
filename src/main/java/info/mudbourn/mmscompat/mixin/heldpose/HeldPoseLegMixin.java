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
 * Lets a Better Combat animation own the legs while the player is standing still,
 * and hands them back to DetailedAnimations the moment the player is doing
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
 * <p>Whenever a Better Combat animation is actually driving the model — either a
 * swing is playing, or {@link HeldPoseMixin} decided this frame that the held item
 * has a pose worth preserving. Reusing that second decision rather than re-deriving
 * it keeps one definition of "a posed weapon is in hand": all of the exclusions that
 * mixin already makes (crossbow, spyglass, trident, any item in use) come along for
 * free, because each of them clears {@code mms_held_pose} on its way out.
 *
 * <p>That ordering is why this mixin outranks {@link HeldPoseMixin} in priority.
 * Both inject at {@code RETURN} of the same method, and a later-applied mixin's
 * callback lands after an earlier one's, so the higher number runs second and reads
 * a store that is already current for this frame instead of lagging it by one.
 * Nothing here writes to the model, so running last is otherwise harmless.
 *
 * <h2>When it yields</h2>
 *
 * <p>{@code walkAnimationSpeed} is the amplitude vanilla and DA both scale limb
 * swing by. It decays to zero when the player stops and while airborne, so one test
 * covers standing still, hovering and the tail of a landing. Switching authority at
 * the moment it reaches zero is what makes the handover invisible: DA's contribution
 * is nothing at that instant, so there is nothing to pop away from.
 *
 * <p>Two states need testing separately because they do not move that value:
 * crouching is a held pose rather than an idle, and water has its own leg cycle that
 * runs whether or not the player is making headway — treading water is stationary by
 * that measure but is emphatically not standing. Any contact with water yields, which
 * also covers standing in the shallows; DA's idle legs there are a far smaller
 * artefact than a dry-land stance played while submerged.
 */
@Mixin(value = PlayerModel.class, priority = 3000)
public class HeldPoseLegMixin {

    private static final String SOURCE = HeldPoseSource.LEG_SOURCE;

    /** The addon's own key, read only as a probe for "a swing is playing". */
    private static final String BETTER_COMBAT_SOURCE = "better_combat";

    /** Below this the limb animation counts as stopped and the pose takes over. */
    private static final float LIMBS_SETTLED = 0.01F;

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
        boolean posed = PoseManager.getSavedPoses(uuid, BETTER_COMBAT_SOURCE) != null
                || PoseManager.getSavedPoses(uuid, HeldPoseSource.SOURCE) != null;

        boolean locomotionActive = player.isCrouching()
                || player.isInWater()
                || player.isVisuallySwimming()
                || state.walkAnimationSpeed > LIMBS_SETTLED;

        if (!posed || locomotionActive) {
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
}
