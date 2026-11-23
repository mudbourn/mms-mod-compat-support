package info.mudbourn.mmscompat.client.alexscaves;

import com.mojang.blaze3d.platform.InputConstants;
import info.mudbourn.mmscompat.alexscaves.WornPossession;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

// The possess key: hold it to use a Totem of Possession worn in a trinket slot. Unbound by default.
public final class WornPossessionKey {
    private static KeyMapping key;
    private static boolean wasDown;

    private WornPossessionKey() {
    }

    public static void register() {
        key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.mms_compat.worn_possession", InputConstants.UNKNOWN.getValue(), mmsCategory()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean down = client.player != null && key.isDown();
            if (down != wasDown && client.getConnection() != null) {
                ClientPlayNetworking.send(new WornPossession.KeyC2S(down));
            }
            wasDown = down;
        });
    }

    private static KeyMapping.Category mmsCategory() {
        Identifier id = Identifier.fromNamespaceAndPath("mms", "main");
        try {
            return KeyMapping.Category.register(id);
        } catch (IllegalArgumentException alreadyRegistered) {
            return new KeyMapping.Category(id);
        }
    }
}
