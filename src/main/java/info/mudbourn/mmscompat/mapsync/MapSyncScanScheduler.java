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

// Runs MapSyncer's incremental region scan off the server thread
public final class MapSyncScanScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat/mapsync");

    // Seconds the scan thread waits for the pre-scan world save
    private static final long SAVE_TIMEOUT_SECONDS = 120L;

    // Single-threaded scan executor
    private static final ExecutorService SCAN_POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mms-mapsync-scan");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });

    // True while a scan is queued or running
    private static final AtomicBoolean SCAN_IN_FLIGHT = new AtomicBoolean(false);

    private MapSyncScanScheduler() {}

    // Queues the incremental scan on the background thread
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
                LOGGER.error("[mms_compat] incremental scan failed off-thread", t);
            } finally {
                SCAN_IN_FLIGHT.set(false);
            }
        });
    }

    // Runs the pre-scan world save on the server thread and waits for it
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
