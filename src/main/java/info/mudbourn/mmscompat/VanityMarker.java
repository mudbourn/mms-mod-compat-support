package info.mudbourn.mmscompat;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

/**
 * The component that says "this stack is cosmetic", for effects that cannot be
 * removed any other way.
 *
 * <h2>Why a component and not the lore line</h2>
 *
 * <p>{@link VanityUtil} already stamps a grey {@code (Vanity)} lore line, and for a
 * while that was the only thing distinguishing a vanity piece. Lore is display text:
 * any mod, command or anvil can rewrite it, and a gameplay gate keyed to a string a
 * player can edit is not a gate. This is a real component — persisted, synced, and
 * meaningless to anything that does not look for it.
 *
 * <h2>When it is the answer, and when it is not</h2>
 *
 * <p>It is the <em>last</em> resort, not the first. Three cheaper things come before
 * it, in order:
 *
 * <ol>
 *   <li><b>Remove the component.</b> Effects that are data — attributes, enchantments,
 *       {@code GLIDER} for elytra flight — come off the stack and nothing else is
 *       needed.</li>
 *   <li><b>Skin it onto another item.</b> A leather base wearing another mod's
 *       equipment asset looks identical and cannot match a check for that mod's item,
 *       so the effect never fires. Costs no coupling to the mod at all.</li>
 *   <li><b>This.</b> Only for effects keyed to something a skin cannot change — an
 *       item tag, or an item whose look is drawn from the item itself. Every use is a
 *       mixin into a mod's internals that will need revisiting when that mod
 *       updates.</li>
 * </ol>
 *
 * <p>Two things need it today: Frostiful's ice skates, whose effect keys off the
 * {@code frostiful:ice_skates} item tag — tag membership belongs to the item, so no
 * component or skin changes it — and Mutant Monsters' skull, which is a block drawn
 * from its block model and so cannot be skinned onto a helmet without losing its
 * shape.
 */
public final class VanityMarker {

    public static final DataComponentType<Unit> COMPONENT = DataComponentType.<Unit>builder()
        .persistent(Unit.CODEC)
        .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
        .build();

    /** Registers the component. Call once, from mod init, before any kit is handed out. */
    public static void register() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath("mms_compat", "vanity"), COMPONENT);
    }

    /**
     * True if this stack was handed out by the vanity command.
     *
     * <p>Null-safe and empty-safe, because the callers are mixins reading equipment
     * slots, where an empty stack is the common case.
     */
    public static boolean isVanity(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.has(COMPONENT);
    }

    private VanityMarker() {
    }
}
