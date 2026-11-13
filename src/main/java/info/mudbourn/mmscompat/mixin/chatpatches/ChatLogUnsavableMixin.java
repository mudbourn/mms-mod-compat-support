package info.mudbourn.mmscompat.mixin.chatpatches;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chat Patches mixin: keeps messages that cannot be encoded out of the chat log so one bad message does not fail every save.
 */
@Mixin(targets = "obro1961.chatpatches.ChatLog")
public class ChatLogUnsavableMixin {

    @Shadow
    @Final
    public static Codec<Pair<ObjectList<Component>, ObjectList<String>>> CODEC;

    @Inject(method = "addMessage", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$skipUnsavable(Component message, CallbackInfo ci) {
        Pair<ObjectList<Component>, ObjectList<String>> probe = Pair.of(
                ObjectArrayList.of(message),
                new ObjectArrayList<>()
        );
        try {
            var level = Minecraft.getInstance().level;
            var ops = level == null ? JsonOps.INSTANCE : level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
            if (CODEC.encodeStart(ops, probe).isError()) {
                ci.cancel();
            }
        } catch (RuntimeException e) {
            ci.cancel();
        }
    }
}
