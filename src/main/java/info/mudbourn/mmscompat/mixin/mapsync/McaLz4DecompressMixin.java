package info.mudbourn.mmscompat.mixin.mapsync;

import net.jpountz.lz4.LZ4BlockInputStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Decodes LZ4 region chunks (compression type 4) in MapSyncer's region reader, which otherwise
 * skips them and sends map tiles with those chunks blank.
 */
@Mixin(targets = "com.mapsyncer.mca.McaReader", remap = false)
public class McaLz4DecompressMixin {

    @Inject(method = "decompress", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$decompressLz4(byte[] data, int compressionType, CallbackInfoReturnable<byte[]> cir)
            throws IOException {
        if (compressionType != 4) {
            return;
        }
        try (InputStream in = new LZ4BlockInputStream(new ByteArrayInputStream(data))) {
            cir.setReturnValue(in.readAllBytes());
        }
    }
}
