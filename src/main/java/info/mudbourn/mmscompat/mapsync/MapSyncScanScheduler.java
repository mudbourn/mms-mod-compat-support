package info.mudbourn.mmscompat.mapsync;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Runs MapSyncer's incremental region scan off the server thread.
 *
 * <p>MapSyncer's SCHEDULED mode calls {@code ConversionOrchestrator.performIncrementalScan}
 * straight from {@code ServerTickEvents.END_SERVER_TICK}. That scan walks every dirty region
 * file through {@code McaReader}/{@code RegionConverterStandalone} — on MMSLive01 that was 278
 * overworld regions — and blocks the tick for well past {@code max-tick-time}. The watchdog
 * killed the server at 04:01 on three consecutive nights (crash reports 2026-07-24, -25, -26),
 * every stack landing inside the MCA reader.
 *
 * <p>The scan is almost entirely file I/O against region files on disk; the only part that
 * genuinely needs the server thread is the {@code saveEverything} call it makes first, which is
 * bounced back via {@link #saveOnServerThread}. See the mapsync mixins for the two redirects.
 */
public final class MapSyncScanScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat/mapsync");

    /**
     * How long the scan thread will wait for the server thread to run the pre-scan world save
     * before giving up. The save itself is normally sub-second; a wait this long means the
     * server thread is wedged or shutting down, and blocking forever would leak this thread.
     */
    private static final long SAVE_TIMEOUT_SECONDS = 120L;

    /**
     * Single-threaded on purpose: two concurrent scans would race on the timestamp cache and the
     * Xaero region output. Daemon so a wedged scan can never hold up JVM shutdown.
     */
    private static final ExecutorService SCAN_POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mms-mapsync-scan");
        t.setDaemon(true);
        // Below the server thread: the whole point is that this must never starve the tick.
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });

    /** Guards against queueing a second scan while one is still running. */
    private static final AtomicBoolean SCAN_IN_FLIGHT = new AtomicBoolean(false);

    private MapSyncScanScheduler() {}

    /**
     * Queues the incremental scan on the background thread and returns immediately, so the tick
     * that triggered it completes normally.
     *
     * @param server the server the scan was requested for
     * @param scan   the original {@code performIncrementalScan} call, invoked off-thread
     */
    public static void submit(MinecraftServer server, Consumer<MinecraftServer> scan) {
        if (!SCAN_IN_FLIGHT.compareAndSet(false, true)) {
            LOGGER.warn("[mms_compat] incremental scan still running, skipping this trigger");
            return;
        }
        LOGGER.info("[mms_compat] dispatching MapSyncer incremental scan off the server thread");
        SCAN_POOL.execute(() -> {
            long startedAt = System.nanoTime();
            try {
                scan.accept(server);
                long seconds = (System.nanoTime() - startedAt) / 1_000_000_000L;
                LOGGER.info("[mms_compat] incremental scan finished in {}s (off-thread)", seconds);
            } catch (Throwable t) {
                // Swallow: this thread is detached from the tick, so an escape would only kill
                // the pool thread silently and leave the flag stuck.
                LOGGER.error("[mms_compat] incremental scan failed off-thread", t);
            } finally {
                SCAN_IN_FLIGHT.set(false);
            }
        });
    }

    /**
     * Runs the pre-scan world save on the server thread and waits for it.
     *
     * <p>Called from the scan thread, where invoking {@code saveEverything} directly would touch
     * chunk storage from the wrong thread. If we are already on the server thread (the scan ran
     * inline for some reason) this just calls through.
     */
    public static boolean saveOnServerThread(MinecraftServer server, boolean suppressLogs,
                                             boolean flush, boolean forced) {
        if (server.isSameThread()) {
            return server.saveEverything(suppressLogs, flush, forced);
        }
        CompletableFuture<Boolean> onTick =
                CompletableFuture.supplyAsync(() -> server.saveEverything(suppressLogs, flush, forced), server);
        try {
            return onTick.get(SAVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            onTick.cancel(false);
            LOGGER.warn("[mms_compat] timed out after {}s waiting for the server thread to save "
                    + "before the incremental scan; scanning against on-disk state as-is",
                    SAVE_TIMEOUT_SECONDS);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            LOGGER.error("[mms_compat] pre-scan save failed on the server thread", e);
            return false;
        }
    }
}
