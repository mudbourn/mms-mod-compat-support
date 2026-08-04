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
 * <p>Reconciliation adds shared entries that are missing and corrects the ones
 * already present — in place, by name — to the server's coordinates, colour,
 * initials and yaw. It removes only names the server knew and has since dropped.
 * A name the server has never heard of is left alone entirely.
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
        // they still hold it. Creating a second copy would show it twice, so a
        // name the player holds in one of their own sets is never *added* to the
        // MMS set. It is still corrected if it is already in there — see below.
        Set<String> ownNames = ownWaypointNames(world);

        WaypointSet set = world.getWaypointSet(SharedWaypoints.SET_NAME);
        if (set == null) {
            if (serverList.isEmpty()) return;
            set = WaypointSet.Builder.begin().setName(SharedWaypoints.SET_NAME).build();
            world.addWaypointSet(set);
        }

        Set<String> mirrored = MIRRORED.computeIfAbsent(dimension, k -> new HashSet<>());
        Map<String, Entry> wantedByName = new HashMap<>();
        for (Entry e : serverList) {
            wantedByName.put(e.name().toLowerCase(Locale.ROOT), e);
        }

        int added = 0;
        int moved = 0;
        int dropped = 0;

        // Pass 1 — reconcile what is already in the set, by name.
        //
        // Anything matching a shared entry is corrected in place to the server's
        // coordinates, colour, initials and yaw. Matching by name rather than by
        // our own provenance record is what makes this self-healing: MIRRORED
        // starts empty every session, and the old code only ever touched names it
        // had added during *this* session, so a waypoint the server had since
        // moved stayed wrong at its old spot forever — and an admin's own
        // waypoint that got published could never be reconciled with the shared
        // copy at all. Updating in place also means Xaero keeps the same waypoint
        // object, so a player's per-waypoint settings survive the correction.
        //
        // Adopting a name this way does put it under the mirror's control for the
        // rest of the session, so /mmswp remove will later take it out of the set.
        // That is the intended reading: a waypoint sitting in the shared set under
        // a shared name *is* the shared waypoint. A name the server does not know
        // is never touched, which is what keeps a waypoint the player just made in
        // the MMS set — Xaero files new waypoints into the selected set — alive.
        var iter = set.getWaypoints().iterator();
        while (iter.hasNext()) {
            Waypoint w = iter.next();
            String key = w.getName().toLowerCase(Locale.ROOT);
            Entry want = wantedByName.remove(key);

            if (want == null) {
                if (mirrored.remove(key)) {
                    iter.remove();  // unpublished server-side
                    dropped++;
                }
                continue;
            }

            mirrored.add(key);
            if (applyTo(w, want)) moved++;
        }

        // Pass 2 — add shared entries the set does not have yet.
        for (Entry e : wantedByName.values()) {
            String key = e.name().toLowerCase(Locale.ROOT);
            if (ownNames.contains(key)) continue;   // player holds their own copy elsewhere
            Waypoint w = new Waypoint(e.x(), e.y(), e.z(), e.name(), e.initials(),
                    WaypointColor.fromIndex(e.colorIdx()), WaypointPurpose.NORMAL);
            w.setYaw(e.yaw());
            w.setRotation(true);
            set.add(w);
            mirrored.add(key);
            added++;
        }

        if (added == 0 && moved == 0 && dropped == 0) return;

        try {
            session.getWorldManagerIO().saveWorld(world);
        } catch (Exception e) {
            LOGGER.error("failed to save Xaero world after shared waypoint sync", e);
        }
        LOGGER.info("shared waypoint sync for {}: {} added, {} corrected, {} removed (set '{}')",
                dimension, added, moved, dropped, SharedWaypoints.SET_NAME);
    }

    /**
     * Bring one mirrored waypoint in line with the server's copy, in place.
     * Returns true if anything actually changed.
     *
     * <p>Visibility is deliberately not written. It is the player's own
     * render-distance setting, and it is also what {@link XaeroGlobalWaypointBridge}
     * reads to decide what a scan publishes — forcing mirrors to GLOBAL would make
     * every mirror a scan candidate and let a stale client push old coordinates
     * back onto the server.
     */
    private static boolean applyTo(Waypoint w, Entry e) {
        boolean changed = false;
        if (w.getX() != e.x()) { w.setX(e.x()); changed = true; }
        if (w.getY() != e.y()) { w.setY(e.y()); changed = true; }
        if (w.getZ() != e.z()) { w.setZ(e.z()); changed = true; }
        if (w.getColor() != e.colorIdx()) { w.setColor(e.colorIdx()); changed = true; }
        if (!e.initials().equals(w.getSymbol())) { w.setSymbol(e.initials()); changed = true; }
        if (w.getYaw() != e.yaw()) { w.setYaw(e.yaw()); changed = true; }
        return changed;
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
