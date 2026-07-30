package info.mudbourn.mmscompat;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Sound events owned by this mod.
 *
 * <p>Better Combat resolves a {@code swing_sound} id against the sound-event
 * registry, so a bare resource pack entry isn't enough — the event has to be
 * registered. Registration happens on both sides because the registry is
 * synced to clients on join; the {@code .ogg} files behind it are client-only
 * assets and are ignored server-side.
 */
public final class MmsSounds {

    /**
     * Dry stone impact for the fossilised tuna. Three variants live under
     * {@code assets/mms_compat/sounds.json}, so the game picks one per swing
     * and repeated hits don't sound identical.
     */
    public static final Identifier STONE_IMPACT_ID =
            Identifier.fromNamespaceAndPath("mms_compat", "stone_impact");

    public static final SoundEvent STONE_IMPACT = SoundEvent.createVariableRangeEvent(STONE_IMPACT_ID);

    private MmsSounds() {}

    public static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, STONE_IMPACT_ID, STONE_IMPACT);
    }
}
