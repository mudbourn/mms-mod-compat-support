package info.mudbourn.mmscompat.mixin.neasprint;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tr7zw.notenoughanimations.animations.hands.ClampCrossbowAnimations;
import dev.tr7zw.notenoughanimations.animations.hands.CustomBowAnimation;
import dev.tr7zw.notenoughanimations.animations.hands.VanillaProjectileWeaponAnimation;
import dev.tr7zw.notenoughanimations.animations.vanilla.VanillaShieldAnimation;
import dev.tr7zw.notenoughanimations.animations.vanilla.VanillaSingleHandedAnimation;
import dev.tr7zw.notenoughanimations.animations.vanilla.VanillaTwoHandedAnimation;
import dev.tr7zw.notenoughanimations.api.BasicAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds Not Enough Animations' held-item arm poses to the set that survives
 * DetailedAnimations, so an equipped item keeps its NEA pose while the player moves.
 *
 * <h2>Why they were being dropped</h2>
 *
 * <p>EMF Compat: NEA decides what beats the CEM animation in one place —
 * {@code EMFCompat.shouldPauseForAnimation}, a chain of {@code instanceof} tests.
 * Read out of the bytecode, the list is: boat, horse, eat/drink, hug, item swap,
 * map holding, look-at-item, pet, naruto run, burning and freezing. Everything else
 * returns false, is never stored in {@code PoseManager}, and is therefore overwritten
 * by DA's arms inside {@code ModelPart#render}.
 *
 * <p>Every animation NEA drives from <em>what is in the hand</em> falls in that
 * "everything else": the single- and two-handed carries, the shield, the bow and the
 * crossbow. Those are exactly the poses that visibly collapse the moment the player
 * moves, because DA's idle arms happen to be close enough to NEA's at a standstill
 * that the loss only shows once DA starts swinging them.
 *
 * <p>Note this is a different bug from the one {@link NeaSprintMixin} fixes, in the
 * same addon. That one is a blanket clear on sprinting that discarded poses the
 * whitelist had already approved; this is the whitelist never approving them at all.
 * Removing the sprint rule could not fix these, because they were dropped a step
 * earlier.
 *
 * <h2>What is added, and what deliberately is not</h2>
 *
 * <p>Only the six hand animations chosen by the held item. The full-body entries —
 * swimming, crawling, the ladder, elytra, riptide, falling, sleep — are left alone.
 * They are postures rather than item holds, they move legs and body as well as arms,
 * and DA has its own versions of all of them; letting both write would be a fight
 * with no held item to justify it.
 *
 * <h2>Known conflict</h2>
 *
 * <p>An NEA held-item pose and a Better Combat held pose can now both want the arms
 * for the same item, and {@code PoseManager} merges sources in {@code HashMap} order
 * — so which one wins is arbitrary. That is accepted for now: the immediate goal is
 * that the arms stop falling on movement, and the arbitration between NEA, Better
 * Combat and Inspect Animations is a separate decision about which pose should win,
 * not about whether the pose survives at all.
 *
 * <h2>Fragility</h2>
 *
 * <p>Same shape as {@link NeaSprintMixin}: this targets an addon-internal static, not
 * a public API. {@code ModifyReturnValue} only ever widens the answer from false to
 * true, so an upstream change that adds these classes to the whitelist itself makes
 * this a no-op rather than a double-apply.
 */
@Mixin(targets = "strm.neaemfcompat.compat.EMFCompat", remap = false)
public class NeaHeldItemWhitelistMixin {

    @ModifyReturnValue(method = "shouldPauseForAnimation", at = @At("RETURN"), require = 1)
    private static boolean mms$keepHeldItemPoses(boolean original, BasicAnimation animation) {
        if (original) {
            return true;
        }
        return animation instanceof VanillaSingleHandedAnimation
                || animation instanceof VanillaTwoHandedAnimation
                || animation instanceof VanillaShieldAnimation
                || animation instanceof CustomBowAnimation
                || animation instanceof ClampCrossbowAnimations
                || animation instanceof VanillaProjectileWeaponAnimation;
    }
}
