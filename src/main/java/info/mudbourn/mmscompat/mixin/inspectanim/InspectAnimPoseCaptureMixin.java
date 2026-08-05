package info.mudbourn.mmscompat.mixin.inspectanim;

import info.mudbourn.mmscompat.client.InspectAnimPoseBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers Inspect Animations' arm pose with emf_compat_core so EMF doesn't
 * erase it.
 *
 * <p>Inspect Animations poses the arm from {@code HumanoidModel#poseLeftArm} /
 * {@code poseRightArm}, both of which run inside {@code setupAnim}. EMF applies
 * a resource-pack player model afterwards, from {@code ModelPart#render}, and
 * overwrites the arm rotations outright — so the raised arm is gone by the time
 * the model is drawn, while the item keeps animating off its own PoseStack.
 * Snapshotting here, at the end of {@code setupAnim}, hands the pose to
 * emf_compat_core, which re-applies it at the end of {@code animate()}.
 *
 * <p>Priority matches emf_compat_better_combat's own capture mixin so the two
 * sources behave alike; they use distinct source keys and PoseManager merges
 * them, so relative order between the two doesn't matter.
 *
 * @see InspectAnimPoseBridge
 */
@Mixin(value = PlayerModel.class, priority = 2500)
public abstract class InspectAnimPoseCaptureMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At("RETURN"))
    private void mmsCompat$captureInspectArmPose(AvatarRenderState state, CallbackInfo ci) {
        String animation = InspectAnimPoseBridge.animationName(state);
        if (animation == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !(mc.level.getEntity(state.id) instanceof AbstractClientPlayer player)) {
            return;
        }

        if ("NONE".equals(animation) || "RANDOM".equals(animation)) {
            InspectAnimPoseBridge.clear(player.getUUID());
            return;
        }

        // An inspect is an idle flourish, so anything with a real animation of its
        // own outranks it. Vetoing the render state rather than declining to
        // capture is what also stops the held item spinning; see
        // InspectAnimPoseBridge#suppress.
        if (info.mudbourn.mmscompat.client.InspectAnimGate.suppressed(player)) {
            InspectAnimPoseBridge.suppress(state);
            InspectAnimPoseBridge.clear(player.getUUID());
            return;
        }

        PlayerModel self = (PlayerModel) (Object) this;
        // FLOURISH passes the item hand to hand, so both arms are posed. The
        // rest touch only the main arm — leave the other under EMF's control
        // so its idle animation keeps running.
        if ("FLOURISH".equals(animation)) {
            InspectAnimPoseBridge.capture(player.getUUID(), self.leftArm, self.rightArm);
        } else if (state.mainArm == HumanoidArm.RIGHT) {
            InspectAnimPoseBridge.capture(player.getUUID(), null, self.rightArm);
        } else {
            InspectAnimPoseBridge.capture(player.getUUID(), self.leftArm, null);
        }
    }
}
