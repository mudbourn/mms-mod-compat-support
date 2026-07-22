package info.mudbourn.mmscompat.mixin.metrofix;

import com.example.modmetro.MetroMod;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes riders sitting on top of metro carts instead of inside them.
 *
 * ModMetro registers its cart as:
 *     EntityType.Builder.of(MetroCartEntity::new, MobCategory.MISC)
 *         .sized(0.98f, 0.7f)
 *         .build(key)
 *
 * The hitbox matches vanilla, but the builder omits .passengerAttachments().
 * Since 1.20.5 the seat position comes from EntityAttachment.PASSENGER on the
 * entity's dimensions; with no explicit attachment the default is
 * height * 0.75 = 0.525, versus vanilla minecart's 0.1875. That ~0.34b lift
 * is what puts the player on the roof.
 *
 * MetroCartEntity overrides no passenger-positioning method, so there is
 * nothing to override — the value has to be injected at registration.
 *
 * clientTrackingRange is also absent (inheriting the default 5 rather than
 * vanilla minecart's 8), which makes trains pop in late on approach. Fixed
 * here too since it is the same builder chain.
 *
 * EntityType-level, so already-spawned carts pick this up on restart with
 * no world editing.
 *
 * NOTE: METRO_CART is a {@code static final} field, so the builder chain runs
 * in the class's static initializer ({@code <clinit>}), NOT {@code onInitialize}.
 * The redirect must target {@code <clinit>} and the handler must be static to
 * match that static context.
 */
@Mixin(MetroMod.class)
public abstract class MetroCartSeatMixin {

    @Redirect(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityType$Builder;sized(FF)"
                   + "Lnet/minecraft/world/entity/EntityType$Builder;"
        )
    )
    private static EntityType.Builder<?> mmsCompat$seatRiderInsideCart(
            EntityType.Builder<?> builder, float width, float height) {
        return builder.sized(width, height)
                .passengerAttachments(0.1875f)
                .clientTrackingRange(8);
    }
}
