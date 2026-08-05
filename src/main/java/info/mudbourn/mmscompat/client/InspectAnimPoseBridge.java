package info.mudbourn.mmscompat.client;

import net.minecraft.client.model.geom.ModelPart;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

/**
 * Reflective bridge that registers Inspect Animations as a pose source with
 * emf_compat_core's PoseManager.
 *
 * <p>Why this exists: EMF's {@code EMFModelPartRoot#animate()} runs from
 * {@code ModelPart#render}, i.e. strictly <em>after</em> {@code setupAnim} has
 * returned, and a resource-pack player model (DetailedAnimations ships one, at
 * {@code assets/minecraft/emf/cem/player.jem}) assigns {@code right_arm} /
 * {@code left_arm} rotations unconditionally. Anything that posed the arms
 * during {@code setupAnim} is therefore erased before the model is drawn.
 *
 * <p>Inspect Animations splits its work across both sides of that line: its
 * {@code AnimateArmPoses} mixin writes the raised arm onto the ModelPart (lost
 * to EMF), while {@code AnimateThirdPerson} transforms the held item's PoseStack
 * (untouched by EMF). The result is an item flipping and spinning off a hand
 * that never leaves the player's side.
 *
 * <p>emf_compat_core already implements the fix — it snapshots arm poses per
 * player UUID and re-applies them at the end of {@code animate()}, merging every
 * registered source in {@code PoseManager#getSavedPoses(UUID)}. Better Combat
 * registers itself via emf_compat_better_combat. Nothing registers Inspect
 * Animations, so we do it here.
 *
 * <p>All access is reflective so we take no compile-time dependency on either
 * mod, and any signature change upstream degrades to leaving the pose stock
 * rather than crashing.
 */
public final class InspectAnimPoseBridge {

    /** Source key under which we stash poses. Must not collide with "default". */
    public static final String SOURCE = "inspect_animations";

    private static final String POSE_MANAGER = "strm.emfcompat.core.PoseManager";
    private static final String POSE_SNAPSHOT = "strm.emfcompat.core.PoseSnapshot";
    private static final String ANIMATION_STATE = "net.soundsofthesun.inspectanimations.iface.IAnimationState";
    private static final String ANIMATION = "net.soundsofthesun.inspectanimations.Animation";
    private static final String GET_ANIMATION = "inspectanimations$getAnimation";
    private static final String SET_ANIMATION = "inspectanimations$setAnimation";

    private static volatile boolean unavailable;
    private static volatile MethodHandle snapshotCtor;
    private static volatile MethodHandle savePoses;
    private static volatile MethodHandle clearPoses;
    private static volatile MethodHandle getAnimation;
    private static volatile MethodHandle setAnimation;
    private static volatile Object noneConstant;

    private InspectAnimPoseBridge() {
    }

    /**
     * Reads the render state's current inspect animation.
     *
     * @return the animation's enum constant name ({@code TURN}, {@code FLIP},
     *         {@code TOSS}, {@code FLOURISH}, {@code NONE}), or null if Inspect
     *         Animations isn't present or has been reshaped.
     */
    public static String animationName(Object renderState) {
        if (!resolve() || renderState == null) {
            return null;
        }
        try {
            Object animation = getAnimation.invoke(renderState);
            return animation instanceof Enum<?> e ? e.name() : null;
        } catch (Throwable t) {
            unavailable = true;
            return null;
        }
    }

    /**
     * Forces this frame's render state to {@code NONE}, which is how an inspect is
     * suppressed without touching Inspect Animations' own scheduling.
     *
     * <p>Both of the mod's consumers read the animation off the render state —
     * {@code AnimateArmPoses} for the raised arm and {@code AnimateThirdPerson} for
     * the held item's PoseStack — so clearing it here stops the item spinning as
     * well as the arm lifting. Clearing only our {@code PoseManager} capture would
     * stop the arm and leave the item flipping off a hand that never moved, which
     * is the exact artefact {@link InspectAnimPoseBridge} was written to fix.
     *
     * <p>Safe to do per frame: {@code AvatarRenderer#extractRenderState} rebuilds
     * the render state's animation from the player every frame, so this is a
     * per-frame veto and not a write to the mod's source of truth. The inspect
     * keeps running underneath and reappears the moment the gate opens, rather than
     * being cancelled and needing to be re-triggered.
     */
    public static void suppress(Object renderState) {
        if (!resolve() || renderState == null || noneConstant == null) {
            return;
        }
        try {
            setAnimation.invoke(renderState, noneConstant);
        } catch (Throwable t) {
            unavailable = true;
        }
    }

    /**
     * Snapshots the given arms so emf_compat_core restores them after EMF has
     * animated. Either arm may be null to leave that side under EMF's control.
     */
    public static void capture(UUID uuid, ModelPart leftArm, ModelPart rightArm) {
        if (!resolve()) {
            return;
        }
        try {
            savePoses.invoke(uuid, SOURCE,
                    leftArm == null ? null : snapshotCtor.invoke(leftArm),
                    rightArm == null ? null : snapshotCtor.invoke(rightArm));
        } catch (Throwable t) {
            unavailable = true;
        }
    }

    /** Drops our snapshot so EMF resumes full control of the arms. */
    public static void clear(UUID uuid) {
        if (!resolve()) {
            return;
        }
        try {
            clearPoses.invoke(uuid, SOURCE);
        } catch (Throwable t) {
            unavailable = true;
        }
    }

    private static boolean resolve() {
        if (unavailable) {
            return false;
        }
        if (savePoses != null) {
            return true;
        }
        synchronized (InspectAnimPoseBridge.class) {
            if (unavailable) {
                return false;
            }
            if (savePoses != null) {
                return true;
            }
            try {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                ClassLoader loader = InspectAnimPoseBridge.class.getClassLoader();

                Class<?> snapshotClass = Class.forName(POSE_SNAPSHOT, false, loader);
                Class<?> managerClass = Class.forName(POSE_MANAGER, false, loader);
                Class<?> stateClass = Class.forName(ANIMATION_STATE, false, loader);

                MethodHandle ctor = lookup.unreflectConstructor(
                        snapshotClass.getConstructor(ModelPart.class));
                MethodHandle save = lookup.unreflect(managerClass.getMethod(
                        "savePoses", UUID.class, String.class, snapshotClass, snapshotClass));
                MethodHandle drop = lookup.unreflect(managerClass.getMethod(
                        "clearPoses", UUID.class, String.class));
                MethodHandle anim = lookup.unreflect(stateClass.getMethod(GET_ANIMATION));

                Class<?> animationClass = Class.forName(ANIMATION, false, loader);
                MethodHandle setAnim = lookup.unreflect(
                        stateClass.getMethod(SET_ANIMATION, animationClass));
                Object none = Enum.valueOf(animationClass.asSubclass(Enum.class), "NONE");

                // Erase the mod-owned types so callers never need them on the stack.
                snapshotCtor = ctor.asType(ctor.type().changeReturnType(Object.class));
                clearPoses = drop;
                noneConstant = none;
                setAnimation = setAnim.asType(setAnim.type()
                        .changeParameterType(0, Object.class)
                        .changeParameterType(1, Object.class));
                getAnimation = anim.asType(anim.type()
                        .changeParameterType(0, Object.class)
                        .changeReturnType(Object.class));
                savePoses = save.asType(save.type()
                        .changeParameterType(2, Object.class)
                        .changeParameterType(3, Object.class));
                return true;
            } catch (Throwable t) {
                // Either mod absent or reshaped — stand down, leave poses stock.
                unavailable = true;
                return false;
            }
        }
    }
}
