package info.mudbourn.mmscompat.mixin.biscuitroll;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes the actual cause of the "Ticking entity" NPE inside Biscuit Roll's
 * animation lookup whenever a resource reload is in flight (F3+T, or toggling
 * a resource pack) with a Useless Reptile mob loaded.
 *
 * <p>{@code URDragonEntityModelProvider} takes an early exit while resources
 * are reloading, because the variant data it would otherwise consult is not
 * available yet:</p>
 *
 * <pre>
 *   public Identifier getAnimationId(BRState renderState) {
 *       Identifier dragonId = renderState.getStateData(DRAGON_ID);
 *       if (!ResourceUtil.isResourceReloadFinished) {
 *           return this.getDefaultModel(dragonId);   // &lt;-- wrong default
 *       }
 *       ...
 * </pre>
 *
 * <p>That early return hands back the <em>model</em> default
 * ({@code .../moleclaw/moleclaw.geo.json}) where the <em>animation</em> default
 * ({@code .../moleclaw/moleclaw.animation.json}) belongs — a copy-paste slip
 * from the identical {@code getModelId} below it. The controller stores that id
 * as its {@code animationFile}, and the geo id is of course not a key in the
 * animation registry, so:</p>
 *
 * <pre>
 *   AnimationData[] data = this.getAnimations(animationId);  // null
 *   return Arrays.stream(data)                              // NPE
 * </pre>
 *
 * <pre>
 *   java.lang.NullPointerException: Cannot read the array length because "array" is null
 *     at nordmods.biscuit_roll.common.util.BRAnimationManager.getAnimation(BRAnimationManager.java:69)
 *     at nordmods.uselessreptile.common.entity.Moleclaw.tickTurnController(Moleclaw.java:175)
 * </pre>
 *
 * <p>Redirecting that one call to {@code getDefaultAnimation} gives the lookup a
 * key that is genuinely registered, so the mob animates from its default file
 * for the duration of the reload and then picks its variant up again.</p>
 *
 * <p>This is the real fix; {@link BiscuitRollAnimationReloadMixin} is still
 * worth keeping, but on its own it could never have helped — the id being
 * looked up was never in the registry to begin with, cleared or not.</p>
 *
 * <p>Targeted by string because Useless Reptile is not a compile-time
 * dependency. Remapping must stay on: the class name and the method names pass
 * through the remapper untouched, but both descriptors name
 * {@code Identifier}, which is {@code class_2960} at runtime.</p>
 */
@Mixin(targets = "nordmods.uselessreptile.client.model_provider.URDragonEntityModelProvider")
public abstract class UselessReptileReloadAnimationIdMixin {

    @Shadow
    protected abstract Identifier getDefaultAnimation(Identifier entity);

    @Redirect(
        method = "getAnimationId",
        at = @At(
            value = "INVOKE",
            target = "Lnordmods/uselessreptile/client/model_provider/URDragonEntityModelProvider;"
                   + "getDefaultModel(Lnet/minecraft/resources/Identifier;)"
                   + "Lnet/minecraft/resources/Identifier;"
        )
    )
    private Identifier mmsCompat$defaultAnimationNotDefaultModel(
            UselessReptileReloadAnimationIdMixin self, Identifier entity) {
        return this.getDefaultAnimation(entity);
    }
}
