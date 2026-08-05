package info.mudbourn.mmscompat.client;

import net.minecraft.client.player.AbstractClientPlayer;

/**
 * When an Inspect Animations inspect is allowed to play, and who yields to whom
 * when it is.
 *
 * <h2>The ordering problem this avoids</h2>
 *
 * <p>Inspect, Better Combat and DetailedAnimations all want the arms, and only the
 * first two go through {@code PoseManager}. DA loses to either by construction —
 * {@code EMFModelPartRootMixin} re-applies the store after the CEM animation has
 * run — so "inspect beats DA" needs nothing.
 *
 * <p>Inspect versus Better Combat is the real contest, and raising a mixin priority
 * would not settle it: {@code PoseManager#getSavedPoses(UUID)} merges sources by
 * iterating a {@code HashMap}'s values and letting the last non-null arm win. That
 * order is stable per run but arbitrary, decided by string hash rather than by
 * anyone's intent — so today's winner is luck, not priority, and no amount of
 * {@code @Mixin(priority = ...)} touches it.
 *
 * <p>So the contest is removed instead of won. At most one of the two producers is
 * allowed to be storing arms on any given frame:
 *
 * <ul>
 *   <li>An inspect never plays while the player is swinging, drawing, rolling or
 *       crawling — see {@link #suppressed}. Those are the states Better Combat and
 *       Combat Roll have real animations for, and interrupting them with an idle
 *       flourish is the thing that looks broken.</li>
 *   <li>Outside those states, an inspect that <em>is</em> playing makes
 *       {@code HeldPoseMixin} release its idle hold — see {@link #isInspecting}.
 *       The hold is only describing how the weapon rests, which is exactly what an
 *       inspect is overriding.</li>
 * </ul>
 *
 * <p>Both producers therefore write under a condition the other's condition
 * excludes, and the merge order stops mattering.
 */
public final class InspectAnimGate {

    private InspectAnimGate() {
    }

    /**
     * Whether an inspect must not play for this player on this frame.
     *
     * <p>The four states, and why each is read the way it is:
     *
     * <ul>
     *   <li><b>Swinging</b> — Better Combat's {@code bettercombat:attack} PAL layer,
     *       active for the whole swing. Read through
     *       {@link PlayerAnimLayerProbe} rather than through {@code PoseManager}'s
     *       {@code better_combat} key, because that key is written during
     *       {@code setupAnim} and would be a frame stale to anything asking
     *       earlier.</li>
     *   <li><b>Cocking back or loading</b> — {@code isUsingItem} covers the bow
     *       draw, the crossbow load, the trident charge and the shield raise in one
     *       test. "In use" for a crossbow means loading, which is the case the
     *       user's phrasing is pointing at.</li>
     *   <li><b>Rolling</b> — Combat Roll's {@code combat_roll:roll} PAL layer.</li>
     *   <li><b>Crawling</b> — {@code isVisuallyCrawling}, the prone pose itself
     *       rather than the input that caused it, so it stays true in a
     *       one-block gap the player did not choose to crawl into.</li>
     * </ul>
     */
    public static boolean suppressed(AbstractClientPlayer player) {
        return player.isUsingItem()
                || player.isVisuallyCrawling()
                || PlayerAnimLayerProbe.isPlaying(player, PlayerAnimLayerProbe.BETTER_COMBAT_ATTACK)
                || PlayerAnimLayerProbe.isPlaying(player, PlayerAnimLayerProbe.COMBAT_ROLL);
    }

    /**
     * Whether an inspect is playing and allowed to, which is the signal for the
     * held-pose producer to stand down.
     *
     * @param animation the enum constant name from
     *                  {@link InspectAnimPoseBridge#animationName}, or null
     */
    public static boolean isInspecting(AbstractClientPlayer player, String animation) {
        if (animation == null || "NONE".equals(animation) || "RANDOM".equals(animation)) {
            return false;
        }
        return !suppressed(player);
    }
}
