package info.mudbourn.mmscompat.mixin.heldpose;

import info.mudbourn.mmscompat.client.HeldPoseSource;
import info.mudbourn.mmscompat.client.PoseDebug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import strm.emfcompat.core.PoseManager;

import java.util.UUID;

/**
 * Opens the debug frame and takes the first arm sample, at the end of
 * {@code setupAnim}.
 *
 * <p>This is the pre-CEM reading: vanilla's arms, plus whatever Player Animation Lib
 * applied for Better Combat, plus whatever Not Enough Animations applied. It is the
 * baseline every later sample is compared against.
 *
 * <p>The frame is opened at {@code HEAD} rather than {@code RETURN} because that is
 * the one ordering guarantee available without resolving the priority question — a
 * method's {@code HEAD} always precedes its own {@code RETURN}, whatever order the
 * mixins themselves ended up in. The previous frame's samples are flushed there too,
 * so nothing has to know which sample point runs last.
 *
 * <p>Priority is above every producer in this package so the {@code RETURN} sample
 * has the best chance of reading the final pre-CEM state. If the ordering rule turns
 * out to be the opposite one, the trace will show it rather than hide it — that is
 * the point of tagging samples instead of assuming positions.
 */
@Mixin(value = PlayerModel.class, priority = 4000)
public class PoseDebugSetupMixin {

    @Inject(method = "setupAnim", at = @At("HEAD"))
    private void mms$openDebugFrame(AvatarRenderState state, CallbackInfo ci) {
        if (!PoseDebug.enabled) {
            return;
        }
        if (!mms$isLocal(state)) {
            return;
        }
        UUID local = Minecraft.getInstance().player.getUUID();

        // Source ownership is read here, before this frame's producers run, so the
        // header describes the store the restore will actually consult.
        PoseDebug.beginFrame(String.format(
                "pose trace  held=%s legs=%s bc=%s inspect=%s",
                has(local, HeldPoseSource.SOURCE),
                has(local, HeldPoseSource.LEG_SOURCE),
                has(local, "better_combat"),
                has(local, "inspect_animations")));
    }

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void mms$sampleSetupAnim(AvatarRenderState state, CallbackInfo ci) {
        // The frame is opened only for the local player, but every other player
        // rendered in the same tick reaches this same RETURN while it is still open
        // — so the identity has to be re-checked here or their arms land in the
        // trace as extra, unlabelled setupAnim lines.
        if (!PoseDebug.recording() || !mms$isLocal(state)) {
            return;
        }
        PlayerModel model = (PlayerModel) (Object) this;
        PoseDebug.sample("setupAnim", model.leftArm, model.rightArm);
    }

    private static boolean mms$isLocal(AvatarRenderState state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return false;
        }
        var entity = mc.level.getEntity(state.id);
        return entity != null && mc.player.getUUID().equals(entity.getUUID());
    }

    private static String has(UUID uuid, String source) {
        return PoseManager.getSavedPoses(uuid, source) != null ? "y" : "-";
    }
}
