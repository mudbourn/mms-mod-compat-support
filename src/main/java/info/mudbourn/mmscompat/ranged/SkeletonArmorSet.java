package info.mudbourn.mmscompat.ranged;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * How much of the mutant skeleton armour set someone is wearing.
 *
 * <p>Mutant Monsters keys its own buffs off single pieces — the chestplate for
 * quick draw, the skull for multishot — and never asks about the set as a
 * whole. MMS wants a drawback that scales with commitment to the set, so this
 * counts pieces instead.
 *
 * <p>Matched by registry id rather than by the mod's item class, so this stays
 * loadable with Mutant Monsters absent; with the mod gone nothing resolves and
 * the count is always zero.
 */
public final class SkeletonArmorSet {

    /** Pieces that must be worn before the set's drawbacks kick in. */
    public static final int DRAWBACK_THRESHOLD = 3;

    private static final Identifier SKULL =
            Identifier.fromNamespaceAndPath("mutantmonsters", "mutant_skeleton_skull");
    private static final Identifier CHESTPLATE =
            Identifier.fromNamespaceAndPath("mutantmonsters", "mutant_skeleton_chestplate");
    private static final Identifier LEGGINGS =
            Identifier.fromNamespaceAndPath("mutantmonsters", "mutant_skeleton_leggings");
    private static final Identifier BOOTS =
            Identifier.fromNamespaceAndPath("mutantmonsters", "mutant_skeleton_boots");

    private SkeletonArmorSet() {}

    /** How many of the four set pieces {@code entity} has equipped, 0 to 4. */
    public static int wornPieces(@Nullable LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        int worn = 0;
        if (is(entity.getItemBySlot(EquipmentSlot.HEAD), SKULL)) worn++;
        if (is(entity.getItemBySlot(EquipmentSlot.CHEST), CHESTPLATE)) worn++;
        if (is(entity.getItemBySlot(EquipmentSlot.LEGS), LEGGINGS)) worn++;
        if (is(entity.getItemBySlot(EquipmentSlot.FEET), BOOTS)) worn++;
        return worn;
    }

    /** Whether enough of the set is worn for its drawbacks to apply. */
    public static boolean atDrawbackThreshold(@Nullable LivingEntity entity) {
        return wornPieces(entity) >= DRAWBACK_THRESHOLD;
    }

    private static boolean is(ItemStack stack, Identifier id) {
        return !stack.isEmpty() && id.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
