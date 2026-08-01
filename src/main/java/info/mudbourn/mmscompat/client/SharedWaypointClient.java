package info.mudbourn.mmscompat.client;

import info.mudbourn.mmscompat.waypoint.SharedWaypoints;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.Entry;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.SyncS2C;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client half of the shared-waypoint sync.
 *
 * Caches the server's authoritative per-dimension lists, and reconciles the
 * CURRENT dimension's list into a real Xaero waypoint set ("MMS") — one that
 * shows up in Xaero's own waypoint menu, minimap, and world map like any
 * player-made set. Players control rendering (in-world beacons etc.) with
 * Xaero's normal settings; the set can be hidden per-player from the menu.
 *
 * <h2>The MMS set is not wiped</h2>
 *
 * <p>Reconciliation is additive. It adds shared entries that are missing, moves
 * ones the server has relocated, and removes only names it put there itself and
 * the server has since dropped. Anything else in the set is left alone.
 *
 * <p>This matters because the MMS set is a real Xaero set, so it can be the
 * <em>selected</em> set — and Xaero files a newly made waypoint into whichever
 * set is selected. The old reconcile rebuilt the set from scratch every three
 * seconds, which deleted any waypoint a player made while MMS was selected,
 * usually within seconds of making it. Deleting a shared waypoint from the set
 * by hand still does not unpublish it; it comes back on the next sync, and the
 * only unpublish is /mmswp remove.
 */
public final class SharedWaypointClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat_wp_client");
    private static final int RECONCILE_INTERVAL_TICKS = 60; // 3s

    /** dimension id -> latest server list. */
    private static final Map<String, List<Entry>> CACHE = new ConcurrentHashMap<>();

    /**
     * dimension id -> lowercased names this client has mirrored into the MMS set.
     *
     * <p>The provenance record that lets removal stay narrow. A name in here came
     * from the server, so dropping it when the server drops it is just keeping the
     * mirror honest; a name that is not in here was made by the player and is
     * never touched. Starting empty each session is deliberate — after a restart
     * the client would rather leave a stale shared waypoint in place than risk
     * deleting one somebody made.
     */
    private static final Map<String, Set<String>> MIRRORED = new ConcurrentHashMap<>();

    private static int tickCounter = 0;

    private SharedWaypointClient() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SyncS2C.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    CACHE.put(payload.dimension(), List.copyOf(payload.entries()));
                    tickCounter = RECONCILE_INTERVAL_TICKS; // reconcile on next tick
                }));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CACHE.clear();
            MIRRORED.clear();
        });

        ClientTickEvents.END_CLIENT_TICK.register(SharedWaypointClient::tick);
    }


    private static void tick(Minecraft client) {
        if (++tickCounter < RECONCILE_INTERVAL_TICKS) return;
        tickCounter = 0;

        if (client.player == null || client.level == null) return;

        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) return;
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        if (world == null) return;

        String dimension = client.player.level().dimension().identifier().toString();
        List<Entry> serverList = CACHE.get(dimension);
        if (serverList == null) return; // no server data (yet) — leave whatever exists alone

        // Publishing copies rather than moves (see XaeroGlobalWaypointBridge), so
        // a player's own GLOBAL waypoint comes back down in the shared list while
        // they still hold it. Mirroring it as well would show it twice, so skip
        // any entry whose name the player already has in one of their own sets.
        // If they later delete their copy, the shared one reappears on the next
        // reconcile — the mirror stays self-healing.
        Set<String> ownNames = ownWaypointNames(world);
        List<Entry> wanted = new ArrayList<>();
        for (Entry e : serverList) {
            if (!ownNames.contains(e.name().toLowerCase(Locale.ROOT))) {
                wanted.add(e);
            }
        }

        WaypointSet set = world.getWaypointSet(SharedWaypoints.SET_NAME);
        if (set == null) {
            if (wanted.isEmpty()) return;
            set = WaypointSet.Builder.begin().setName(SharedWaypoints.SET_NAME).build();
            world.addWaypointSet(set);
        }

        Set<String> mirrored = MIRRORED.computeIfAbsent(dimension, k -> new HashSet<>());
        Map<String, Entry> wantedByName = new HashMap<>();
        for (Entry e : wanted) {
            wantedByName.put(e.name().toLowerCase(Locale.ROOT), e);
        }

        int added = 0;
        int dropped = 0;
        Set<String> movedKeys = new HashSet<>();

        // Pass 1 — drop or correct what we previously mirrored. Waypoints we did
        // not put here are skipped outright, which is what keeps a player's own
        // waypoint alive when the MMS set happens to be their selected set.
        var iter = set.getWaypoints().iterator();
        while (iter.hasNext()) {
            Waypoint w = iter.next();
            String key = w.getName().toLowerCase(Locale.ROOT);
            if (!mirrored.contains(key)) continue;

            Entry want = wantedByName.get(key);
            if (want == null) {
                iter.remove();          // unpublished server-side, or the player now holds their own
                mirrored.remove(key);
                dropped++;
            } else if (w.getX() != want.x() || w.getY() != want.y() || w.getZ() != want.z()) {
                iter.remove();          // re-added below at the server's coordinates
                movedKeys.add(key);
            } else {
                wantedByName.remove(key); // already correct, nothing to add
            }
        }

        // Pass 2 — add everything still missing, including the moved ones.
        for (Entry e : wantedByName.values()) {
            String key = e.name().toLowerCase(Locale.ROOT);
            // A name the player already uses inside the MMS set is theirs; adding
            // the shared one too would leave two waypoints stacked on one name.
            if (hasWaypoint(set, key)) continue;
            Waypoint w = new Waypoint(e.x(), e.y(), e.z(), e.name(), e.initials(),
                    WaypointColor.fromIndex(e.colorIdx()), WaypointPurpose.NORMAL);
            w.setYaw(e.yaw());
            w.setRotation(true);
            set.add(w);
            mirrored.add(key);
            if (!movedKeys.contains(key)) added++;
        }

        int moved = movedKeys.size();
        if (added == 0 && moved == 0 && dropped == 0) return;

        try {
            session.getWorldManagerIO().saveWorld(world);
        } catch (Exception e) {
            LOGGER.error("failed to save Xaero world after shared waypoint sync", e);
        }
        LOGGER.info("shared waypoint sync for {}: {} added, {} moved, {} removed (set '{}')",
                dimension, added, moved, dropped, SharedWaypoints.SET_NAME);
    }

    private static boolean hasWaypoint(WaypointSet set, String lowerName) {
        for (Waypoint w : set.getWaypoints()) {
            if (w.getName().toLowerCase(Locale.ROOT).equals(lowerName)) return true;
        }
        return false;
    }

    /** Lowercased names of every waypoint the player holds outside the shared set. */
    private static Set<String> ownWaypointNames(MinimapWorld world) {
        Set<String> names = new HashSet<>();
        for (WaypointSet set : world.getIterableWaypointSets()) {
            if (SharedWaypoints.SET_NAME.equals(set.getName())) continue;
            for (Waypoint w : set.getWaypoints()) {
                names.add(w.getName().toLowerCase(Locale.ROOT));
            }
        }
        return names;
    }

}
