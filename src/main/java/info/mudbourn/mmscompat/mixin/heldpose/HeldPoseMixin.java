package info.mudbourn.mmscompat.mixin.heldpose;

import info.mudbourn.mmscompat.client.CrossbowPose;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import strm.emfcompat.core.PoseManager;
import strm.emfcompat.core.PoseSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps a Better Combat held pose from being overwritten by DetailedAnimations'
 * idle arms — the reason a two-handed weapon is carried one-handed with the other
 * arm swinging free.
 *
 * <h2>Why the pose loses</h2>
 *
 * <p>Better Combat applies the {@code pose} from a weapon's
 * {@code weapon_attributes} as a Player Animation Lib animation, which lands on
 * the model during {@code setupAnim}. EMF runs the CEM animation strictly later,
 * from inside {@code ModelPart#render}, so whatever DA writes to {@code left_arm}
 * and {@code right_arm} is simply the last write and wins. This is not a priority
 * that can be raised from the pack side: the two never compete for the same slot.
 *
 * <p>EMF Compat: Core already solves it, in {@code EMFModelPartRootMixin} — it
 * hooks {@code EMFModelPartRoot#animate} at {@code RETURN} and re-applies poses
 * stashed in {@link PoseManager} over the top of the animation that just ran.
 * Anything in that store beats DA by construction.
 *
 * <p>What is missing is a producer. The Better Combat addon only stashes arms
 * while {@code getAttackHand(player) != null} — an <em>attack</em> is playing —
 * and calls {@code clearPoses} the rest of the time. Swings therefore survive DA
 * and idle holds do not. This fills that gap under its own source key, so the two
 * producers never contend for one slot in the store.
 *
 * <h2>What it captures</h2>
 *
 * <p>Both arms, whenever the held weapon declares a {@code pose} or an
 * {@code off_hand_pose}, whether or not it is two-handed — a one-handed weapon
 * with a custom idle has exactly the same problem. The snapshot is taken at
 * {@code RETURN} of {@code setupAnim}, matching the addon's own injection point
 * and priority, which is late enough that PAL has applied the pose.
 *
 * <p>Restoring is not this class's job and first person is not its concern:
 * {@code EMFModelPartRootMixin} skips the local player's first-person pass, so the
 * held-item arm keeps whatever the first-person path gave it.
 */
@Mixin(value = PlayerModel.class, priority = 2500)
public class HeldPoseMixin {

    /**
     * Kept distinct from the addon's {@code "better_combat"} key. {@code PoseManager}
     * merges sources per part, last writer winning, so an attack capture and a held
     * capture in the same frame agree — both read the same posed model — rather than
     * one clearing the other.
     */
    private static final String SOURCE = info.mudbourn.mmscompat.client.HeldPoseSource.SOURCE;

    /**
     * Below this, the limb animation counts as stopped and Better Combat takes
     * full authority. Not a blend threshold — the yield is all-or-nothing, because
     * a partial handover would need DA's post-animation value, which is only
     * readable from a second seam inside EMF's animate. That was tried in
     * 0.9.51-0.9.53 and made things worse.
     */
    private static final float LIMBS_SETTLED = 0.01F;

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void mms$captureHeldPose(AvatarRenderState state, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(state.id);
        if (!(entity instanceof AbstractClientPlayer player)) {
            return;
        }

        PlayerModel model = (PlayerModel) (Object) this;

        // Crossbows are not DA's and not Better Combat's: CrossbowPose owns them for
        // the whole time they are held, in use or not, because "in use" for a
        // crossbow means loading. Release this source so the two never contend.
        if (CrossbowPose.apply(player, model)) {
            PoseManager.clearPoses(player.getUUID(), SOURCE);
            return;
        }

        // An idle hold is exactly that — idle. While the item is actually in use,
        // DetailedAnimations owns the arms: its BowShootingPose and BlockingPose are
        // full-body use animations, and replaying the resting hold over them erases
        // the draw entirely (`expanded_weaponry:longbow` inherits
        // `bettercombat:bow_two_handed_heavy`, which declares
        // `bettercombat:pose_two_handed_bow`, so it lands here on every frame of a
        // draw; the vanilla bow has no Better Combat attributes at all, which is why
        // only the modded bows showed it).
        if (player.isUsingItem()) {
            PoseManager.clearPoses(player.getUUID(), SOURCE);
            return;
        }

        // DA yields only when its own locomotion animation has nothing to say.
        // While the player is actually moving or crouching, DA's gait outranks the
        // Better Combat pose and this stores nothing at all.
        if (mms$locomotionActive(player, state)) {
            PoseManager.clearPoses(player.getUUID(), SOURCE);
            return;
        }

        if (mms$hasCustomPose(player.getMainHandItem()) || mms$hasCustomPose(player.getOffhandItem())) {
            // Legs as well as arms: EMF Compat's Better Combat addon only ever saves
            // the two arms, so DA was the unconditional last writer on legs and
            // toheee1234's ~1600 leg keyframes per animation never reached the
            // screen. Its applier already handles these four part names.
            Map<String, PoseSnapshot> parts = new HashMap<>();
            parts.put("left_leg", new PoseSnapshot(model.leftLeg));
            parts.put("right_leg", new PoseSnapshot(model.rightLeg));
            parts.put("left_pants", new PoseSnapshot(model.leftPants));
            parts.put("right_pants", new PoseSnapshot(model.rightPants));

            PoseManager.savePoses(player.getUUID(), SOURCE,
                    new PoseSnapshot(model.leftArm), new PoseSnapshot(model.rightArm), parts);
        } else {
            // Dropping the weapon has to release the arms in the same frame, or
            // the last posed snapshot is replayed over DA's idle forever.
            PoseManager.clearPoses(player.getUUID(), SOURCE);
        }
    }

    /**
     * Whether DA's locomotion animation is actively driving the limbs.
     *
     * <p>{@code walkAnimationSpeed} is the amplitude vanilla and DA both scale
     * their limb swing by. It decays toward zero when the player stops, and while
     * airborne, so a single test covers standing still, hovering and the tail of a
     * landing — "until my arms and legs stop moving entirely" is literally this
     * value reaching zero.
     *
     * <p>Switching authority exactly when it hits zero is what makes the handover
     * invisible: DA's contribution at that instant is nothing, so there is nothing
     * to pop away from. It also removes the reason the old absolute snapshot was
     * wrong — the vanilla walk swing it used to capture is scaled by this same
     * value, so at the moment of capture that contamination is zero by
     * construction, and no delta subtraction is needed.
     *
     * <p>Crouching is excluded separately: it is a deliberate pose the player is
     * holding, not an idle, and it does not move the walk animation.
     */
    private static boolean mms$locomotionActive(AbstractClientPlayer player, AvatarRenderState state) {
        return player.isCrouching() || state.walkAnimationSpeed > LIMBS_SETTLED;
    }

    private static boolean mms$hasCustomPose(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        WeaponAttributes attributes = WeaponRegistry.getAttributes(stack);
        if (attributes == null) {
            return false;
        }
        return mms$isPose(attributes.pose()) || mms$isPose(attributes.offHandPose());
    }

    /**
     * A blank pose is not a pose. {@code WeaponAttributes#pose} is a plain String
     * deserialized straight out of {@code weapon_attributes} with no normalization,
     * so a profile that declares {@code "pose": ""} — as {@code bettercombat:trident}
     * does — yields a non-null empty string. A null check alone therefore treats the
     * trident as posed, and Better Combat applies no pose animation for it, so the
     * snapshot taken below captures DetailedAnimations' idle arms and replays them
     * over EMF's animation every frame. That is not a missing hold, it is an actively
     * reinstated swing, and it is why the tridents visibly swing in the air.
     */
    private static boolean mms$isPose(String pose) {
        return pose != null && !pose.isBlank();
    }
}
