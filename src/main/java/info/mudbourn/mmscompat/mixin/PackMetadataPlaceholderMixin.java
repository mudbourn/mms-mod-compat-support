package info.mudbourn.mmscompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import net.minecraft.server.packs.AbstractPackMetadataResources;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Fills unexpanded build placeholders in a pack.mcmeta before it is parsed. */
@Mixin(AbstractPackMetadataResources.class)
public class PackMetadataPlaceholderMixin {

    @Unique
    private static final Pattern MMS$PLACEHOLDER = Pattern.compile("\\$\\{[A-Za-z0-9_.-]+}");

    @WrapOperation(
        method = "loadMetadata",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/ResourceMetadata;fromJsonStream(Ljava/io/InputStream;)Lnet/minecraft/server/packs/resources/ResourceMetadata;"
        )
    )
    private static ResourceMetadata mms$fillPlaceholders(InputStream stream, Operation<ResourceMetadata> original) throws IOException {
        String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        String json = MMS$PLACEHOLDER.matcher(text).replaceAll("64");
        return original.call(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }
}
