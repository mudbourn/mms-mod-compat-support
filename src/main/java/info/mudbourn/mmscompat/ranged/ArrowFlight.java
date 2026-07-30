package info.mudbourn.mmscompat.ranged;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Decides how an arrow should fly, from the weapon that fired it and the gear
 * of whoever pulled the trigger.
 *
 * <p>Two MMS rules live here:
 *
 * <ul>
 *   <li>Longbow arrows drop less, so the longbow is worth carrying over a plain
 *       bow for long shots. Paired with the velocity bump in
 *       {@code LongbowVelocityMixin}, this is what makes it a sniping weapon.</li>
 *   <li>A player wearing the mutant skeleton skull gets reduced drag on
 *       crossbow bolts. Mutant Monsters gives the skull a multishot buff that
 *       only bows can use — crossbows never fire the arrow-loose event it hangs
 *       off — so the crossbow family gets its own buff of equivalent weight
 *       instead of being left out.</li>
 * </ul>
 *
 * <p>Everything is matched by registry id or vanilla class, never by another
 * mod's types, so this class stays loadable with any of those mods absent.
 */
public final class ArrowFlight {

    /** Gravity multiplier for longbow arrows. */
    private static final float LONGBOW_GRAVITY_SCALE = 0.65F;

    /** Drag reduction for longbow arrows, on top of the flatter arc. */
    private static final float LONGBOW_INERTIA_BONUS = 0.25F;

    /** Drag reduction on crossbow bolts while the mutant skeleton skull is worn. */
    private static final float SKULL_CROSSBOW_INERTIA_BONUS = 0.5F;

    private static final Identifier LONGBOW =
            Identifier.fromNamespaceAndPath("expanded_weaponry", "longbow");
    private static final Identifier MUTANT_SKELETON_SKULL =
            Identifier.fromNamespaceAndPath("mutantmonsters", "mutant_skeleton_skull");

    /** Vanilla defaults: full gravity, full drag. */
    public static final float[] VANILLA = {1.0F, 0.0F};

    private ArrowFlight() {}

    /**
     * The {@code {gravityScale, inertiaBonus}} pair for an arrow fired from
     * {@code weapon} by {@code shooter}, or {@link #VANILLA} if no rule applies.
     */
    public static float[] profileFor(@Nullable ItemStack weapon, @Nullable LivingEntity shooter) {
        if (weapon == null || weapon.isEmpty()) {
            return VANILLA;
        }

        if (is(weapon, LONGBOW)) {
            return new float[] {LONGBOW_GRAVITY_SCALE, LONGBOW_INERTIA_BONUS};
        }

        if (shooter != null
                && weapon.getItem() instanceof CrossbowItem
                && is(shooter.getItemBySlot(EquipmentSlot.HEAD), MUTANT_SKELETON_SKULL)) {
            return new float[] {1.0F, SKULL_CROSSBOW_INERTIA_BONUS};
        }

        return VANILLA;
    }

    private static boolean is(ItemStack stack, Identifier id) {
        return !stack.isEmpty() && id.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
