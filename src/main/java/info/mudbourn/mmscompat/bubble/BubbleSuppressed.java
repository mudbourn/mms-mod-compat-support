package info.mudbourn.mmscompat.bubble;

import net.minecraft.resources.Identifier;

import java.util.Set;

/** Datapack files of Bubblellaneous that the Java port replaces. */
public final class BubbleSuppressed {

    private static final String PACK = "bubblellaneous";

    private static final Set<Identifier> FILES = Set.of(
        pack("advancement/player/event/use_block/computer.json"),
        pack("advancement/player/event/use_block/call_bell.json"),
        pack("advancement/player/event/use_block/security_monitor.json"),
        pack("advancement/player/event/use_block/surveillance_camera.json"),
        pack("advancement/player/event/use_block/surveillance_camera/toggle_mode.json"),
        pack("advancement/player/event/use_block/surveillance_camera/turn_left.json"),
        pack("advancement/player/event/use_block/surveillance_camera/turn_right.json"),
        pack("function/player/event/use_block/computer.mcfunction"),
        pack("function/player/event/use_block/call_bell.mcfunction"),
        pack("function/player/event/use_block/security_monitor.mcfunction"),
        pack("function/player/event/use_block/surveillance_camera.mcfunction"),
        pack("function/player/event/use_block/surveillance_camera/toggle_mode.mcfunction"),
        pack("function/player/event/use_block/surveillance_camera/turn_left.mcfunction"),
        pack("function/player/event/use_block/surveillance_camera/turn_left/spin.mcfunction"),
        pack("function/player/event/use_block/surveillance_camera/turn_left/toggle.mcfunction"),
        pack("function/player/event/use_block/surveillance_camera/turn_right.mcfunction"),
        pack("function/player/event/use_block/surveillance_camera/turn_right/spin.mcfunction"),
        place("alarm_block"),
        place("call_bell"),
        place("clipboard"),
        place("computer"),
        place("security_monitor"),
        place("surveillance_camera"),
        place("iron_rising_door")
    );

    private BubbleSuppressed() {}

    public static boolean contains(Identifier file) {
        return FILES.contains(file);
    }

    private static Identifier pack(String path) {
        return Identifier.fromNamespaceAndPath(PACK, path);
    }

    private static Identifier place(String name) {
        return Identifier.fromNamespaceAndPath("place", "function/bubblellaneous/" + name + ".mcfunction");
    }
}
