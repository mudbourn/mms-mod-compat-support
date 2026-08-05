package info.mudbourn.mmscompat.mixin.neasprint;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Stops sprinting from discarding Not Enough Animations' arm poses.
 *
 * <h2>The behaviour</h2>
 *
 * <p>EMF Compat: NEA decides each frame which NEA animations are allowed to survive
 * DetailedAnimations, via a whitelist in {@code EMFCompat.shouldPauseForAnimation}
 * — boat, horse, eat/drink, hug, item swap, map holding, look-at-item, pet, naruto
 * run, burning, freezing. Anything on it is stored in {@code PoseManager} and beats
 * the CEM animation; anything else is dropped.
 *
 * <p>Its {@code applyAnimations} tail then adds a second, blanket rule on top:
 *
 * <pre>
 *   if (player.isSprinting() &amp;&amp; !burning &amp;&amp; !freezing &amp;&amp; !narutoRunning &amp;&amp; !horseLegs) {
 *       PoseManager.clearPoses(uuid);
 *       return;
 *   }
 * </pre>
 *
 * <p>So every whitelisted arm pose is thrown away the instant the player sprints,
 * with no blend: the arm cuts from the held pose straight into DA's run cycle in one
 * frame. Visible on any {@code holdingItems} entry — a raised lantern or torch snaps
 * down — and on a held map, which is the same rule reached by a different animation.
 *
 * <h2>The fix</h2>
 *
 * <p>Redirecting that {@code isSprinting()} to {@code false} deletes the blanket rule
 * and nothing else. Control falls through to the check immediately below it, which
 * clears poses only when no whitelisted animation is active — so the whitelist alone
 * decides what survives, sprinting or not, which is what it is for. No pose is kept
 * that would not have been kept while walking.
 *
 * <h2>Fragility</h2>
 *
 * <p>This patches a method that EMF Compat: NEA itself merged into NEA's class, so it
 * is tied to that addon's internals rather than to a public API: the method name is
 * the addon's own {@code neaemfcompat$}-prefixed handler. {@code require = 1} makes an
 * upstream reshape fail loudly at apply time instead of silently reverting the fix —
 * a mixin that fails to apply is indistinguishable in game from a feature that was
 * never written, which is a lesson this codebase learned the hard way.
 *
 * <p>The higher priority is required, not cosmetic: mixins apply in ascending priority
 * order, so this must sort after the addon's default-priority mixin or the method it
 * targets does not yet exist. Names are unremapped ({@code remap = false}) because the
 * target is a mod class; the {@code isSprinting} reference is therefore written in
 * intermediary by hand.
 */
@Mixin(targets = "dev.tr7zw.notenoughanimations.logic.AnimationProvider", remap = false, priority = 1500)
public class NeaSprintMixin {

    @Redirect(
            method = "neaemfcompat$onApplyAnimationsReturn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_5624()Z"),
            require = 1)
    private boolean mms$ignoreSprint(Object player) {
        return false;
    }
}
