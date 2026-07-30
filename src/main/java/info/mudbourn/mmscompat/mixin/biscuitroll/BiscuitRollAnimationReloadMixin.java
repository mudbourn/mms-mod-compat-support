package info.mudbourn.mmscompat.mixin.biscuitroll;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Stops F3+T (resource reload) from crashing the client whenever a Useless
 * Reptile mob is loaded.
 *
 * <p>{@code BRAnimationManager} is a {@code SimplePreparableReloadListener}
 * whose backing store is a plain static {@code HashMap} on
 * {@code ClientAnimationManager}. Its {@code apply} does:</p>
 *
 * <pre>
 *   getHolderMap().clear();
 *   getHolderMap().putAll(map);
 * </pre>
 *
 * <p>and its lookup does no null check at all:</p>
 *
 * <pre>
 *   AnimationData[] data = this.getAnimations(animationId);  // @Nullable
 *   return Arrays.stream(data)                               // NPE
 * </pre>
 *
 * <p>So the registry is emptied unconditionally, and any entity that ticks
 * before it is refilled — or at all, if the incoming map came back short —
 * dereferences a null array:</p>
 *
 * <pre>
 *   java.lang.NullPointerException: Cannot read the array length because "array" is null
 *     at nordmods.biscuit_roll.common.util.BRAnimationManager.getAnimation(BRAnimationManager.java:69)
 *     at nordmods.uselessreptile.common.entity.Wyvern.tickTurnController(Wyvern.java:210)
 * </pre>
 *
 * <p><b>The fix is to skip the {@code clear()}.</b> {@code putAll} already
 * overwrites every key the reload produced, so dropping the clear leaves the
 * registry continuously populated — there is never a window, however brief,
 * where a lookup can see a missing key. It also means that if a reload yields
 * an empty or partial map, the previous known-good animations survive instead
 * of being destroyed.</p>
 *
 * <p>The trade-off: an animation file <em>deleted</em> from a pack lingers in
 * the registry until the game restarts. That is a far better outcome than a
 * hard client crash on every reload, and it only affects the removed-content
 * case, which the pack does not exercise.</p>
 *
 * <p>Targeted by string because Biscuit Roll is not a compile-time dependency.
 * Redirecting the {@code Map.clear()} call rather than cancelling {@code apply}
 * keeps the repopulate intact.</p>
 *
 * <p><b>Remapping must stay on.</b> The target class name and
 * {@code java/util/Map} pass through the remapper untouched, but the
 * {@code apply} descriptor names {@code ResourceManager} and
 * {@code ProfilerFiller} — those are Minecraft classes, and at runtime they are
 * intermediary ({@code class_3300} / {@code class_3695}). With
 * {@code remap = false} the descriptor would keep its Mojmap spelling and match
 * nothing.</p>
 */
@Mixin(targets = "nordmods.biscuit_roll.common.util.BRAnimationManager")
public abstract class BiscuitRollAnimationReloadMixin {

    private static final Logger MMS_COMPAT$LOGGER =
        LoggerFactory.getLogger("mms-compat/biscuit_roll");

    /**
     * No-op in place of {@code getHolderMap().clear()} inside {@code apply}.
     */
    @Redirect(
        method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V")
    )
    private void mmsCompat$keepAnimationsAcrossReload(Map<Object, Object> holder) {
        // Deliberately empty: the following putAll refreshes every key.
    }

    /**
     * Diagnostic only. If a reload ever hands us an empty map, the skipped
     * clear is the thing keeping the client alive — worth saying so in the log
     * rather than leaving it silent.
     */
    @Inject(
        method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("HEAD")
    )
    private void mmsCompat$warnOnEmptyReload(Map<Object, Object> map, ResourceManager resourceManager,
                                             ProfilerFiller profiler, CallbackInfo ci) {
        if (map == null || map.isEmpty()) {
            MMS_COMPAT$LOGGER.warn(
                "Biscuit Roll reload produced no animations; retaining the previous set "
                + "(without this the registry would be emptied and the next entity tick would crash).");
        }
    }
}
