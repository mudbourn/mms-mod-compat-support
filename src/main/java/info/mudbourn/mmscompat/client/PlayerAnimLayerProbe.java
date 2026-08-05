package info.mudbourn.mmscompat.client;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Asks Player Animation Lib whether a named animation layer is currently playing
 * on a given player.
 *
 * <p>Better Combat's attack stack and Combat Roll's roll are both PAL controllers
 * registered under a fixed {@code Identifier}, so one probe answers "is this
 * player mid-swing" and "is this player mid-roll" alike. That is worth more than it
 * looks: both mods drive remote players through the same registration path they use
 * for the local one, so the answer is correct for every rendered player. The
 * obvious alternatives are not — {@code RollManager#isRolling} is only ticked for
 * {@code LocalPlayer}, and would report a permanent "no" for everyone else.
 *
 * <h2>Reflective on purpose</h2>
 *
 * <p>PAL is not a compile-time dependency of this mod and Combat Roll is not a
 * dependency at all. Resolving by name means a missing mod, or a renamed method
 * upstream, degrades to "no layer is playing" — which leaves every caller's
 * behaviour exactly as it was before this class existed — instead of a
 * {@code NoClassDefFoundError} at render time.
 *
 * <p>The handle lookup is cached and the failure latch is one-way, so the cost
 * after the first frame is a map get and an interface call. That matters: this runs
 * per player per frame, and an uncached {@code Class.forName} in that position is
 * how EMF Compat's {@code isActive} ended up at 2.3% of the render thread.
 */
public final class PlayerAnimLayerProbe {

    /** Better Combat's attack animation stack — present for the whole swing. */
    public static final Identifier BETTER_COMBAT_ATTACK =
            Identifier.fromNamespaceAndPath("bettercombat", "attack");

    /** Combat Roll's roll animation — present for the whole roll. */
    public static final Identifier COMBAT_ROLL =
            Identifier.fromNamespaceAndPath("combat_roll", "roll");

    private static final String ACCESS = "com.zigythebird.playeranim.api.PlayerAnimationAccess";
    private static final String ANIMATION = "com.zigythebird.playeranimcore.animation.layered.IAnimation";

    private static volatile boolean unavailable;
    private static volatile MethodHandle getLayer;
    private static volatile MethodHandle isActive;

    /**
     * Layers whose owning mod is absent. {@code getPlayerAnimationLayer} returns
     * null for those every frame; remembering which ids never resolve keeps the
     * common "Combat Roll not installed" case down to a set lookup.
     */
    private static final Map<Identifier, Boolean> MISSING = new ConcurrentHashMap<>();

    private PlayerAnimLayerProbe() {
    }

    /**
     * @return true only if PAL is present, the layer is registered for this player,
     *         and it reports itself active. Any doubt answers false.
     */
    public static boolean isPlaying(Avatar player, Identifier layerId) {
        if (player == null || !resolve() || MISSING.containsKey(layerId)) {
            return false;
        }
        try {
            Object layer = getLayer.invoke(player, layerId);
            if (layer == null) {
                MISSING.put(layerId, Boolean.TRUE);
                return false;
            }
            return (boolean) isActive.invoke(layer);
        } catch (Throwable t) {
            unavailable = true;
            return false;
        }
    }

    private static boolean resolve() {
        if (unavailable) {
            return false;
        }
        if (getLayer != null) {
            return true;
        }
        synchronized (PlayerAnimLayerProbe.class) {
            if (unavailable) {
                return false;
            }
            if (getLayer != null) {
                return true;
            }
            try {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                ClassLoader loader = PlayerAnimLayerProbe.class.getClassLoader();

                Class<?> accessClass = Class.forName(ACCESS, false, loader);
                Class<?> animationClass = Class.forName(ANIMATION, false, loader);

                MethodHandle layer = lookup.unreflect(accessClass.getMethod(
                        "getPlayerAnimationLayer", Avatar.class, Identifier.class));
                MethodHandle active = lookup.unreflect(animationClass.getMethod("isActive"));

                // Erase the mod-owned types so callers never need them on the stack.
                isActive = active.asType(active.type().changeParameterType(0, Object.class));
                getLayer = layer.asType(layer.type().changeReturnType(Object.class));
                return true;
            } catch (Throwable t) {
                unavailable = true;
                return false;
            }
        }
    }
}
