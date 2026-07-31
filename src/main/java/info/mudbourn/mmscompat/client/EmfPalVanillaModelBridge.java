package info.mudbourn.mmscompat.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Makes EMF fall back to the vanilla player model while PlayerAnimationLib is
 * driving an animation, so Better Combat swings actually render in third person.
 *
 * <h2>Why this is needed</h2>
 *
 * <p>EMF already ships the detection half of this. {@code PALCompat} exposes
 * {@code shouldPauseEntityAnim}, which reports whether an entity's PAL
 * {@code AvatarAnimManager} is active, and EMF's own initialiser wires it up:
 *
 * <pre>
 *   if (isThisModLoaded("player_animation_library"))
 *       EMFAnimationApi.registerPauseCondition(PALCompat::shouldPauseEntityAnim);
 *   if (isThisModLoaded("bendable_cuboids"))
 *       EMFAnimationApi.registerVanillaModelCondition(PALCompat::shouldPauseEntityAnim);
 * </pre>
 *
 * <p>Note the two different registrations of the same condition. Without
 * {@code bendable_cuboids} installed — and we do not install it — the condition
 * is registered <em>only</em> as a pause condition. Pausing is the wrong remedy
 * for a pack like DetailedAnimations: {@code EMFModelPartRoot#animate} simply
 * skips {@code animation.run()}, which leaves the pack's custom part tree
 * sitting at the raw offsets authored in the jem. DA's geometry is not
 * self-supporting — its animation is what places the parts at all
 * ({@code body.ty = 22 + ...}, root offsets, parented limb maths) — so a paused
 * DA renders as a collapsed slab with merged limbs, sunk into the floor. That is
 * the mangle, and it gets worse rather than better once PAL starts winning.
 *
 * <p>Forcing the vanilla model instead sidesteps the problem: EMF drops the
 * custom jem for the duration, PAL and Better Combat drive the vanilla player
 * model they were written against, and the swing renders correctly per body
 * part. When PAL goes idle the custom model returns and DA's tweening resumes.
 *
 * <p>This registers exactly the condition EMF would have registered itself had
 * {@code bendable_cuboids} been present, so it is the sanctioned path rather
 * than a mixin fighting EMF's internals.
 *
 * <h2>Why reflection</h2>
 *
 * <p>Neither EMF nor PAL is on this project's compile classpath, and adding two
 * jars to {@code libs/} for a one-call registration is not worth the build
 * churn. Generics are erased, so a {@code Function<Object, Boolean>} satisfies
 * EMF's declared {@code Function<EMFEntity, Boolean>} parameter at runtime. The
 * reflective handles are resolved once at init; the per-frame path is two cached
 * {@code Method#invoke} calls, which is well inside budget for a render-time
 * predicate.
 *
 * <p>Every failure mode degrades to "condition never fires", i.e. exactly
 * today's behaviour, and logs once. Nothing here can break a client that has
 * neither mod.
 */
public final class EmfPalVanillaModelBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat/emf-pal");

    /**
     * Better Combat's transient attack layer, {@code AttackAnimationStack.ID}.
     *
     * <p>This must be queried specifically rather than asking PAL whether
     * <em>anything</em> is playing. Better Combat also keeps four persistent pose
     * layers on PAL — {@code pose_main_hand_body}, {@code pose_main_hand_item},
     * {@code pose_off_hand_body}, {@code pose_off_hand_item} — for as long as a
     * weapon is held, and {@code AnimationStack#isActive()} returns true if
     * <em>any</em> layer is active. Gating on the manager therefore reads as
     * "permanently animating" while armed, which forces the vanilla model
     * continuously and stops the resource pack rendering at all.
     */
    private static final Identifier BC_ATTACK = Identifier.parse("bettercombat:attack");

    /** {@code com.zigythebird.playeranim.accessors.IAnimatedAvatar} */
    private static Class<?> animatedAvatarType;
    /** {@code IAnimatedAvatar#playerAnimLib$getAnimManager()} */
    private static Method getAnimManager;
    /** {@code IAnimatedAvatar#playerAnimLib$getAnimation(Identifier)} */
    private static Method getAnimation;
    /** {@code IAnimation#isActive()}, also inherited by {@code AvatarAnimManager} */
    private static Method isActive;
    /** True when Better Combat is present, i.e. when the pose layers exist. */
    private static boolean betterCombatPresent;

    private EmfPalVanillaModelBridge() {
    }

    public static void register() {
        FabricLoader loader = FabricLoader.getInstance();
        if (!loader.isModLoaded("entity_model_features") || !loader.isModLoaded("player_animation_library")) {
            return;
        }
        if (loader.isModLoaded("bendable_cuboids")) {
            // EMF registers the vanilla-model condition itself in this case;
            // registering a second identical one would just be dead weight.
            LOGGER.info("bendable_cuboids present, EMF registers the PAL vanilla-model condition itself; skipping.");
            return;
        }

        try {
            animatedAvatarType = Class.forName("com.zigythebird.playeranim.accessors.IAnimatedAvatar");
            getAnimManager = animatedAvatarType.getMethod("playerAnimLib$getAnimManager");
            getAnimation = animatedAvatarType.getMethod("playerAnimLib$getAnimation", Identifier.class);
            // isActive() is declared on playeranimcore's IAnimation, which both
            // AnimationStack and the individual layers implement.
            isActive = Class.forName("com.zigythebird.playeranimcore.animation.layered.IAnimation")
                    .getMethod("isActive");
            betterCombatPresent = loader.isModLoaded("bettercombat");

            Class<?> api = Class.forName("traben.entity_model_features.EMFAnimationApi");
            Method registerVanillaModelCondition =
                    api.getMethod("registerVanillaModelCondition", Function.class);

            Function<Object, Boolean> condition = EmfPalVanillaModelBridge::isPalAnimationActive;
            registerVanillaModelCondition.invoke(null, condition);

            LOGGER.info("Registered PAL-active vanilla-model condition with EMF "
                    + "(Better Combat swings now render on the vanilla player model).");
        } catch (Throwable t) {
            LOGGER.warn("Could not register the EMF/PAL vanilla-model condition; "
                    + "third-person swing animations will stay broken. "
                    + "Check whether EMF or PAL renamed their API.", t);
            animatedAvatarType = null;
        }
    }

    /**
     * True while PAL has an active animation for this entity. Mirrors EMF's own
     * {@code PALCompat#shouldPauseEntityAnim}.
     *
     * <p>Called from EMF's render path, so it must never throw: EMF's
     * force-vanilla check wraps listener iteration in a try/catch that falls
     * back to a config default for the whole entity, and an exception here would
     * quietly change unrelated behaviour.
     */
    private static boolean isPalAnimationActive(Object entity) {
        if (animatedAvatarType == null || entity == null || !animatedAvatarType.isInstance(entity)) {
            return false;
        }
        try {
            if (betterCombatPresent) {
                // Only the transient attack layer, never the persistent poses.
                Object attack = getAnimation.invoke(entity, BC_ATTACK);
                return attack != null && Boolean.TRUE.equals(isActive.invoke(attack));
            }
            // No Better Combat means no persistent pose layers to confuse the
            // manager-wide check, so it is safe (and catches PAL emotes).
            Object manager = getAnimManager.invoke(entity);
            return manager != null && Boolean.TRUE.equals(isActive.invoke(manager));
        } catch (Throwable t) {
            return false;
        }
    }
}
