package info.mudbourn.mmscompat.dfu;

import com.mojang.datafixers.DSL;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Pre-builds the DataFixerUpper rewrite rules at server init.
 *
 * <p>Vanilla's dedicated server never calls {@link DataFixers#optimize(Set)} — only
 * {@code net.minecraft.client.main.Main} does, and only for {@code TYPES_FOR_LEVEL_LIST}.
 * {@code net.minecraft.server.Main} just calls {@code getDataFixer()}. That leaves the
 * rewrite rules to be built lazily, on whichever thread first needs a datafix.
 *
 * <p>On MMS that thread is a c2me worldgen worker, mid-tick. Aerial Hell ships ~1,240
 * structure templates at DataVersions as old as 2586 (1.16.2) against a 4671 world, so
 * the {@code structure_starts} generation stage triggers the whole build. Measured cost
 * was 5.7 minutes on one worker with every other worker blocked behind it, which stalls
 * the server thread long enough for the watchdog to kill the server — and starves the
 * host badly enough to take the AMP panel and neighbouring instances down with it.
 *
 * <p>Building the rules up front moves that cost to startup, on background threads,
 * before anyone can connect. Total work is unchanged; only the scheduling differs.
 */
public final class DfuWarmup implements DedicatedServerModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");

    /**
     * STRUCTURE is the one that actually bites (structure templates from mod jars).
     * The chunk types are cheap insurance for the same stall on old saved data.
     */
    private static final Set<DSL.TypeReference> WARMUP_TYPES = Set.of(
            References.STRUCTURE,
            References.CHUNK,
            References.ENTITY_CHUNK,
            References.POI_CHUNK
    );

    @Override
    public void onInitializeServer() {
        final long startNanos = System.nanoTime();
        LOG.info("Pre-building datafixer rewrite rules for {} types", WARMUP_TYPES.size());

        // optimize() dispatches onto the background executor and returns immediately,
        // so this does not block mod init or delay the server binding its port.
        final CompletableFuture<?> future = DataFixers.optimize(WARMUP_TYPES);

        future.whenComplete((ignored, error) -> {
            final long millis = (System.nanoTime() - startNanos) / 1_000_000L;
            if (error != null) {
                LOG.warn("Datafixer warmup failed after {} ms — "
                        + "rules will be built lazily on first use", millis, error);
            } else {
                LOG.info("Datafixer warmup finished in {} ms", millis);
            }
        });
    }
}
