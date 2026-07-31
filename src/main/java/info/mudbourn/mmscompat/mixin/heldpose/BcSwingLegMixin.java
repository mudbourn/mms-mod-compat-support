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
 * Lets a Better Combat <em>swing</em> animation own the legs, but only while
 * DetailedAnimations' locomotion animation has nothing to say.
 *
 * <h2>Scope: swings only</h2>
 *
 * <p>This deliberately does not touch the idle held pose. {@link HeldPoseMixin}
 * applies that unconditionally, because a two-handed weapon should be carried
 * two-handed whether the player is standing, walking or sprinting — gating the
 * hold on movement (tried in 0.9.55) put the weapon back in one hand mid-run,
 * which is the original bug that mixin exists to fix.
 *
 * <p>The yield rule applies to attack animations instead, where it belongs: a
 * swing's footwork should give way to an actual run cycle, and take over again
 * once the player is standing still.
 *
 * <h2>Legs only</h2>
 *
 * <p>Arms during an attack are already covered — EMF Compat's Better Combat addon
 * saves both arms under {@code better_combat} for exactly the duration of a swing.
 * What it never saves is legs, pants or body, so DA is the unconditional last
 * writer there and toheee1234's ~1600 leg keyframes per animation never reach the
 * screen. This fills only that gap.
 *
 * <h2>The gate</h2>
 *
 * <p>{@code walkAnimationSpeed} is the amplitude vanilla and DA both scale limb
 * swing by. It decays to zero when the player stops and while airborne, so one
 * test covers standing still, hovering and the tail of a landing. Switching
 * authority at the moment it reaches zero is what makes the handover invisible:
 * DA's contribution is nothing at that instant, so there is nothing to pop away
 * from. Crouching is excluded separately — it is a held pose, not an idle, and it
 * does not move the walk animation.
 */
@Mixin(value = PlayerModel.class, priority = 500)
public class BcSwingLegMixin {

    private static final String SOURCE = HeldPoseSource.LEG_SOURCE;

    /** The addon's own key, read only as a probe for "a swing is playing". */
    private static final String BETTER_COMBAT_SOURCE = "better_combat";

    /** Below this the limb animation counts as stopped and the swing takes over. */
    private static final float LIMBS_SETTLED = 0.01F;

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void mms$captureSwingLegs(AvatarRenderState state, CallbackInfo ci) {
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
        // "an attack animation is playing" and costs no extra dependency.
        boolean swinging = PoseManager.getSavedPoses(uuid, BETTER_COMBAT_SOURCE) != null;
        boolean locomotionActive = player.isCrouching() || state.walkAnimationSpeed > LIMBS_SETTLED;

        if (!swinging || locomotionActive) {
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
