package info.mudbourn.mmscompat.mapsync;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

// Matches MapSyncer chat lines by translation key, since its lambda names change between builds.
public final class MapSyncChat {

    private MapSyncChat() {
    }

    public static boolean hasKey(Component message, String keyPrefix) {
        if (message.getContents() instanceof TranslatableContents translatable
            && translatable.getKey().startsWith(keyPrefix)) {
            return true;
        }
        for (Component sibling : message.getSiblings()) {
            if (hasKey(sibling, keyPrefix)) {
                return true;
            }
        }
        return false;
    }
}
