package info.mudbourn.mmscompat.mixin.emfcarry;

import strm.emfcompat.core.BodyPartSync;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.models.animation.state.EMFEntityRenderState;
import traben.entity_model_features.models.parts.EMFModelPartRoot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Stops EMF Compat Carry On from capturing a body pose for every entity on
 * every frame when nothing is being carried.
 *
 * <p>The compat mod has two capture sites, and only one of them is gated:</p>
 *
 * <ul>
 *   <li>{@code HumanoidModelMixin} captures the <em>base</em> pose
 *       ({@code BodyPartSync.captureBase}) and correctly guards it with
 *       {@code CarryOnCompat.isCarrying(player)} plus a corpse-dummy check.</li>
 *   <li>{@code EMFModelPartRootMixin} captures the <em>current</em> pose
 *       ({@code BodyPartSync.captureCurrent}) with <b>no guard at all</b>. It
 *       runs on every {@code EMFModelPartRoot} animate, for every entity that
 *       has a CEM model, every frame.</li>
 * </ul>
 *
 * <p>The ungated site is not cheap. Per entity per frame it walks
 * {@code getAllVanillaPartsEMF()} and allocates a {@code String} per part via
 * {@code toStringShort()} to find the one named {@code [vanilla part body]}:</p>
 *
 * <pre>
 *   for (EMFModelPartVanilla part : root.getAllVanillaPartsEMF())
 *       if ("[vanilla part body]".equals(part.toStringShort()))
 *           BodyPartSync.captureCurrent(uuid, "body", part);
 * </pre>
 *
 * <p>In a spark profile taken inside a mob-heavy dungeon that handler was
 * <b>2.59 s of a 59.6 s render thread (4.4%)</b>, 1.26 s of it self time —
 * the seventh-heaviest self frame in the whole profile, above every Sodium
 * and vanilla frame except vertex writing.</p>
 *
 * <p>Every one of those captures is write-only. The single reader,
 * {@code CarryRenderHelperMixin.applyBodyDelta}, looks up
 * {@code BodyPartSync.hasDelta(player.getUUID(), "body")} for the player who
 * is rendering a carried object, and {@code hasDelta} requires <b>both</b> a
 * base and a current snapshot. So a current pose for any UUID that never had
 * a base captured can never be read back — it is pure allocation.</p>
 *
 * <p><b>The gate is therefore "did the base site already accept this entity".</b>
 * A one-map-lookup test on {@code BodyPartSync.getBase(uuid, "body")} inherits
 * the upstream {@code isCarrying} and corpse-dummy conditions exactly, without
 * this mixin needing to resolve a {@code Player} from the render state or to
 * touch any Carry On class itself. Nothing that was previously readable stops
 * being captured, so the feature is unchanged: a carrying player still gets a
 * current pose every frame, because the base site refreshes their base every
 * frame alongside it.</p>
 *
 * <p>The base is written during {@code HumanoidModel.setupAnim}, which runs
 * ahead of the model render that drives {@code EMFModelPartRoot} animation, so
 * the base is already in the map by the time this gate reads it. The worst
 * case is the first frame of a pick-up, where the base may not be there yet
 * and one frame of delta is skipped — invisible at 60 fps, and self-correcting
 * on the next frame.</p>
 *
 * <p>Targeted by the mixin-merged handler name rather than by a seam inside
 * the handler, because a mixin class is not itself loadable — after
 * application the method genuinely exists on {@code EMFModelPartRoot} under
 * the generated name below. That name is stable for a pinned jar (it encodes
 * the compat mod's own build-time mixin id, {@code don000}), and
 * {@code require = 0} means a rebuild that changes it degrades to today's
 * behaviour rather than crashing. {@code priority} is above the default so
 * this applies after the compat mod's mixin, which is what makes the method
 * present to target at all.</p>
 *
 * @see <a href="https://spark.lucko.me/OpzV23rdlV">profile the 4.4% figure comes from</a>
 */
@Mixin(value = EMFModelPartRoot.class, priority = 1500)
public abstract class CarryOnBodyCaptureGateMixin {

    @Inject(
        method = "handler$don000$emf_compat_carry_on$carryonemfcompat$captureCurrentBodyPose",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void mmscompat$skipUnreadableBodyCapture(final CallbackInfo outer, final CallbackInfo ci) {
        final EMFEntityRenderState state = EMFAnimationEntityContext.getEmfState();
        if (state == null || state.emfEntity() == null) {
            ci.cancel();
            return;
        }

        final UUID uuid = state.emfEntity().etf$getUuid();
        if (uuid == null || BodyPartSync.getBase(uuid, "body") == null) {
            // No base snapshot means hasDelta() can never return true for this
            // entity, so whatever we captured here would never be read.
            ci.cancel();
        }
    }
}
