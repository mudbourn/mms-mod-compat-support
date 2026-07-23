package info.mudbourn.mmscompat.mixin.metrofix;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;

/**
 * Rail-topology helper: distances and positions measured ALONG THE TRACK
 * rather than through the air.
 *
 * Every separation figure in ModMetro is a straight-line distance
 * ({@code distanceTo}, {@code toOther.dot(dir)}, the {@code front - dir*spacing}
 * catch-up point). That is only equivalent to track distance on straight
 * track. Through a U-turn a cart four blocks of RAIL behind the car ahead is
 * barely one block of AIR behind it, and every consumer of that number then
 * does the wrong thing — see {@link MetroCurveFollowMixin} for the failure
 * chain that follows.
 *
 * This class replaces the crow-flight metric with a breadth-first walk over
 * connected rail blocks (same neighbour rule ModMetro's own
 * {@code isConnectedByRail} uses: four horizontals, +/-1 in Y, rails only).
 * The search is bounded, and callers only run it when a correction is
 * actually pending, so the per-tick cost stays in the same class as the
 * mod's existing scans.
 */
final class MetroRailPath {

    private MetroRailPath() {
    }

    static boolean isRail(Level world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof BaseRailBlock;
    }

    /**
     * The rail blocks from {@code frontRail} back to {@code followerRail}
     * along the track, index 0 being the front cart's rail and increasing
     * with distance BEHIND it. Optionally continues past the follower by up
     * to {@code extraBehind} further rails, so a cart that must drop back
     * beyond its current position still has somewhere legal to go.
     *
     * Returns null when the two carts are not rail-connected within
     * {@code maxSteps} — the caller then leaves ModMetro's own logic alone
     * rather than guessing.
     */
    static List<BlockPos> spineBehind(Level world, BlockPos followerRail, BlockPos frontRail,
                                      int maxSteps, int extraBehind) {
        List<BlockPos> path = path(world, followerRail, frontRail, maxSteps);
        if (path == null) {
            return null;
        }
        // path runs follower -> front; the spine runs front -> follower.
        List<BlockPos> spine = new ArrayList<>(path);
        java.util.Collections.reverse(spine);
        extendBehind(world, spine, extraBehind);
        return spine;
    }

    /**
     * Track distance in rail blocks between two carts' rails, or -1 if they
     * are not connected within {@code maxSteps}.
     */
    static int distance(Level world, BlockPos fromRail, BlockPos toRail, int maxSteps) {
        List<BlockPos> path = path(world, fromRail, toRail, maxSteps);
        return path == null ? -1 : path.size() - 1;
    }

    /** BFS over connected rails; returns the path inclusive of both ends, or null. */
    private static List<BlockPos> path(Level world, BlockPos from, BlockPos to, int maxSteps) {
        if (from == null || to == null) {
            return null;
        }
        if (from.equals(to)) {
            List<BlockPos> single = new ArrayList<>(1);
            single.add(from);
            return single;
        }
        Map<BlockPos, BlockPos> parent = new HashMap<>();
        Map<BlockPos, Integer> depth = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(from);
        depth.put(from, 0);
        parent.put(from, null);

        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            int d = depth.get(cur);
            if (d >= maxSteps) {
                continue;
            }
            for (BlockPos n : neighbours(world, cur)) {
                if (depth.containsKey(n)) {
                    continue;
                }
                depth.put(n, d + 1);
                parent.put(n, cur);
                if (n.equals(to)) {
                    return rebuild(parent, n);
                }
                queue.add(n);
            }
        }
        return null;
    }

    private static List<BlockPos> rebuild(Map<BlockPos, BlockPos> parent, BlockPos end) {
        List<BlockPos> out = new ArrayList<>();
        for (BlockPos p = end; p != null; p = parent.get(p)) {
            out.add(p);
        }
        java.util.Collections.reverse(out);
        return out;
    }

    /**
     * Walks the spine further back from its tail, away from the front cart.
     * At a junction the branch heading furthest from the front is taken —
     * the alternative is doubling back up the track the train just left.
     */
    private static void extendBehind(Level world, List<BlockPos> spine, int extra) {
        if (extra <= 0 || spine.isEmpty()) {
            return;
        }
        Set<BlockPos> used = new HashSet<>(spine);
        BlockPos front = spine.get(0);
        for (int i = 0; i < extra; i++) {
            BlockPos tail = spine.get(spine.size() - 1);
            BlockPos best = null;
            double bestDist = -1.0;
            for (BlockPos n : neighbours(world, tail)) {
                if (used.contains(n)) {
                    continue;
                }
                double d = n.distSqr(front);
                if (d > bestDist) {
                    bestDist = d;
                    best = n;
                }
            }
            if (best == null) {
                return; // end of track
            }
            used.add(best);
            spine.add(best);
        }
    }

    private static List<BlockPos> neighbours(Level world, BlockPos pos) {
        List<BlockPos> out = new ArrayList<>(6);
        BlockPos[] horizontals = { pos.north(), pos.south(), pos.east(), pos.west() };
        for (BlockPos h : horizontals) {
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos n = h.above(dy);
                if (isRail(world, n)) {
                    out.add(n);
                }
            }
        }
        return out;
    }
}
