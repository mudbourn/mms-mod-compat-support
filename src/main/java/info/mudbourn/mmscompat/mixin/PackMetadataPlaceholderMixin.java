package info.mudbourn.mmscompat.mixin;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.regex.Pattern;
import net.minecraft.server.packs.AbstractPackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fills unexpanded build placeholders in a pack.mcmeta before it is parsed.
 *
 * <p>Some mod jars ship the template form of their pack.mcmeta, for example
 * mru's {@code "pack_format": ${pack_format}}, which is not valid JSON, so the
 * whole pack was dropped with a MalformedJsonException. Each bare
 * {@code ${name}} is replaced with 64, the last single-number client format,
 * which the lenient pack format rules then accept for both pack types.
 */
@Mixin(AbstractPackResources.class)
public class PackMetadataPlaceholderMixin {

    @Unique
    private static final Pattern MMS$PLACEHOLDER = Pattern.compile("\\$\\{[A-Za-z0-9_.-]+}");

    @WrapOperation(
        method = "getMetadataFromStream",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/GsonHelper;parse(Ljava/io/Reader;)Lcom/google/gson/JsonObject;"
        )
    )
    private static JsonObject mms$fillPlaceholders(Reader reader, Operation<JsonObject> original) {
        StringWriter text = new StringWriter();
        try {
            reader.transferTo(text);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        String json = MMS$PLACEHOLDER.matcher(text.toString()).replaceAll("64");
        return original.call(new StringReader(json));
    }
}
