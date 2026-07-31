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
    private static final String SOURCE = "mms_held_pose";

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

        if (mms$hasCustomPose(player.getMainHandItem()) || mms$hasCustomPose(player.getOffhandItem())) {
            PoseManager.savePoses(player.getUUID(), SOURCE,
                    new PoseSnapshot(model.leftArm), new PoseSnapshot(model.rightArm));
        } else {
            // Dropping the weapon has to release the arms in the same frame, or
            // the last posed snapshot is replayed over DA's idle forever.
            PoseManager.clearPoses(player.getUUID(), SOURCE);
        }
    }

    private static boolean mms$hasCustomPose(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        WeaponAttributes attributes = WeaponRegistry.getAttributes(stack);
        return attributes != null && (attributes.pose() != null || attributes.offHandPose() != null);
    }
}
