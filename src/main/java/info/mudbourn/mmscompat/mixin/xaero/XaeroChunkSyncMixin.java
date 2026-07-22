package info.mudbourn.mmscompat.mixin.xaero;

import info.mudbourn.mmscompat.map.ChunkSyncManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Client-side mixin into Xaero's World Map {@code MapProcessor}.
 * After Xaero's processes chunks, we report loaded chunks to the server for sync.
 */
@Environment(EnvType.CLIENT)
@Mixin(targets = "xaero.map.MapProcessor", remap = false)
public abstract class XaeroChunkSyncMixin {

    @Unique private static final Set<Long> mmsCompat$reported = new HashSet<>();
    @Unique private static String mmsCompat$lastDimension = "";
    @Unique private static int mmsCompat$tickCounter = 0;

    @Inject(method = "onRender", at = @At("TAIL"), remap = false)
    private void mmsCompat$afterMapRender(CallbackInfo ci) {
        if (++mmsCompat$tickCounter < 20) return;
        mmsCompat$tickCounter = 0;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        String dimension = mc.level.dimension().toString();

        if (!dimension.equals(mmsCompat$lastDimension)) {
            mmsCompat$reported.clear();
            mmsCompat$lastDimension = dimension;
        }

        int playerChunkX = mc.player.chunkPosition().x;
        int playerChunkZ = mc.player.chunkPosition().z;
        int radius = 12;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = playerChunkX + dx;
                int cz = playerChunkZ + dz;
                long packed = ChunkPos.asLong(cx, cz);

                if (mmsCompat$reported.contains(packed)) continue;
                if (mc.level.getChunkSource().getChunkNow(cx, cz) == null) continue;

                mmsCompat$reported.add(packed);

                ClientPlayNetworking.send(
                        new ChunkSyncManager.ExploredChunkPayload(cx, cz, dimension));
            }
        }
    }
}
