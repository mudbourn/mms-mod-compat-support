package info.mudbourn.mmscompat.mixin.computerpc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Restores normal block-entity culling for ComputerPC displays.
 *
 * <p>{@code DisplayBlockEntityRenderer} opts out of both culling mechanisms
 * vanilla gives a block entity renderer:</p>
 *
 * <pre>
 *   public boolean shouldRenderOffScreen() { return true; }   // ignore the frustum
 *   public int getViewDistance()           { return 128; }    // 2x the vanilla norm
 * </pre>
 *
 * <p>Together those mean every display within 128 blocks has
 * {@code extractRenderState} run every frame whether or not it is on screen,
 * and each of those calls goes through
 * {@code DisplayBrowserManager.getSession} — which resolves the cluster, keeps
 * the session alive, and re-syncs authoritative state. So turning around or
 * backing off gains nothing, which is exactly the symptom.</p>
 *
 * <p>Nothing about a browser display needs off-screen rendering: it is a flat
 * quad sampling a CEF texture, with no shadow, beacon beam, or other effect
 * that has to be drawn while the block itself is out of view. Returning
 * {@code false} lets the frustum check do its job.</p>
 *
 * <p>The distance is cut to {@value #VIEW_DISTANCE} blocks because the screens
 * visibly break up past roughly that far anyway — the texture is a fixed
 * resolution stretched over the cluster, so beyond ~50 blocks it is unreadable
 * mush that still costs a full per-frame extract.</p>
 *
 * <p><b>Both methods are named by intermediary, deliberately.</b> They are
 * overrides of {@code BlockEntityRenderer} interface methods
 * ({@code shouldRenderOffScreen} / {@code getViewDistance}), but the remapper
 * only rewrites a name in an annotation when it can resolve it against a class
 * it has mappings for. The target here is {@code justpc.computerpc.*}, which it
 * knows nothing about, so a Mojmap spelling would be emitted verbatim and match
 * nothing — ComputerPC ships compiled against intermediary, where these really
 * are {@code method_3563} and {@code method_33893}. Both descriptors are
 * {@code ()Z} / {@code ()I} with no Minecraft types, so there is nothing else
 * for the remapper to do.</p>
 *
 * <p>This is the render-thread half of the problem only. The fixed cost of
 * having MCEF loaded at all — {@code N_DoMessageLoopWork()} at the head of
 * {@code GameRenderer.render}, and the preloaded browser pool — is untouched by
 * this mixin and cannot be fixed from here.</p>
 */
@Mixin(targets = "justpc.computerpc.client.render.DisplayBlockEntityRenderer")
public abstract class DisplayCullingMixin {

    /** Distance in blocks past which a display is no longer worth drawing. */
    private static final int VIEW_DISTANCE = 50;

    @Inject(method = "method_3563", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$cullOffScreenDisplays(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "method_33893", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$shortenDisplayViewDistance(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(VIEW_DISTANCE);
    }
}
