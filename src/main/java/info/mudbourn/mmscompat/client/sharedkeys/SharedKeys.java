package info.mudbourn.mmscompat.client.sharedkeys;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Lets origins, Useless Reptile and Alex's Caves share the origins ability keys (G and B by default), routed by
 * what the player is doing:
 * <ul>
 *   <li>riding a Useless Reptile dragon: G and B fire its primary and secondary attacks;</li>
 *   <li>riding an Alex's Caves mount: G fires its special ability;</li>
 *   <li>crouching in Alex's Caves keybind armor (the Darkness armor): crouch + G fires the armor's ability;</li>
 *   <li>otherwise G and B are the origins actives, as before.</li>
 * </ul>
 * In each of the first three cases the origins actives are held off, so one press never fires two things. Each
 * mod's own keys keep working too. The other mods are read by name, so none of them is required.
 */
public final class SharedKeys {

    private static final Class<?> UR_DRAGON = load("nordmods.uselessreptile.common.entity.base.URRideableDragonEntity");
    private static final Class<?> AC_MOUNT = load("com.github.alexmodguy.alexscaves.server.entity.util.KeybindUsingMount");
    private static final Class<?> AC_ARMOR = load("com.github.alexmodguy.alexscaves.server.item.KeybindUsingArmor");
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private static KeyMapping primary;
    private static KeyMapping secondary;
    private static KeyMapping acSpecial;
    private static boolean looked;

    private SharedKeys() {
    }

    /** The origins primary active key (G), or null without origins. */
    public static KeyMapping primary() {
        lookUp();
        return primary;
    }

    /** The origins secondary active key (B), or null without origins. */
    public static KeyMapping secondary() {
        lookUp();
        return secondary;
    }

    /** Alex's Caves' special ability key, or null without it. */
    public static KeyMapping acSpecial() {
        lookUp();
        return acSpecial;
    }

    public static boolean ridingDragon() {
        return isA(vehicle(), UR_DRAGON);
    }

    public static boolean ridingCavesMount() {
        return isA(vehicle(), AC_MOUNT);
    }

    /** Crouching while wearing a piece of Alex's Caves keybind armor. */
    public static boolean crouchingInCavesArmor() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || AC_ARMOR == null || !player.isShiftKeyDown()) {
            return false;
        }
        for (EquipmentSlot slot : ARMOR) {
            if (AC_ARMOR.isInstance(player.getItemBySlot(slot).getItem())) {
                return true;
            }
        }
        return false;
    }

    /** Whether the origins actives are held off because G and B belong to something else right now. */
    public static boolean originsBlocked() {
        return ridingDragon() || ridingCavesMount() || crouchingInCavesArmor();
    }

    /** Whether G counts as Alex's Caves' special ability right now. */
    public static boolean primaryIsCavesSpecial() {
        return ridingCavesMount() || crouchingInCavesArmor();
    }

    public static boolean down(KeyMapping key) {
        return key != null && key.isDown();
    }

    private static Entity vehicle() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : player.getVehicle();
    }

    private static boolean isA(Object o, Class<?> type) {
        return o != null && type != null && type.isInstance(o);
    }

    private static void lookUp() {
        if (looked) {
            return;
        }
        looked = true;
        primary = field("io.github.apace100.origins.OriginsClient", "usePrimaryActivePowerKeybind");
        secondary = field("io.github.apace100.origins.OriginsClient", "useSecondaryActivePowerKeybind");
        acSpecial = field("com.github.alexmodguy.alexscaves.server.misc.ACKeybindRegistry", "KEY_SPECIAL_ABILITY");
    }

    private static KeyMapping field(String owner, String name) {
        try {
            return (KeyMapping) Class.forName(owner).getField(name).get(null);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    private static Class<?> load(String name) {
        try {
            return Class.forName(name, false, SharedKeys.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }
}
