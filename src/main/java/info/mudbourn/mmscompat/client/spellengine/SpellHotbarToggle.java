package info.mudbourn.mmscompat.client.spellengine;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * The spell hotbar toggle. Each press of the key (Left Alt by default) switches number keys
 * 1-9 between casting spells and switching hotbar slots. It starts off, on slots.
 */
public final class SpellHotbarToggle {
    private static KeyMapping key;
    private static boolean on;

    private SpellHotbarToggle() {}

    public static boolean on() {
        return on;
    }

    public static void register() {
        key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.mms_compat.spell_hotbar_toggle",
            InputConstants.KEY_LALT,
            mmsCategory()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (key.consumeClick()) {
                on = !on;
            }
        });
    }

    // The shared MMS controls category. Every mms mod registers it, and the first one wins.
    private static KeyMapping.Category mmsCategory() {
        Identifier id = Identifier.fromNamespaceAndPath("mms", "main");
        try {
            return KeyMapping.Category.register(id);
        } catch (IllegalArgumentException alreadyRegistered) {
            return new KeyMapping.Category(id);
        }
    }
}
