package info.mudbourn.mmscompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerFunctionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Skips load-tag functions whose only job is a "loaded" tellraw to every player.
 *
 * <p>Superior Stonecutter's {@code superior_stonecutter:load} is a single
 * {@code tellraw @a} line. The load tag runs on the first normal tick after a
 * reload, which on MMSLive01 is when the first player joins an idle server, so
 * it read as a login message.
 */
@Mixin(ServerFunctionManager.class)
public class LoadFunctionMuteMixin {

    @Unique
    private static final Set<Identifier> MMS$MUTED = Set.of(
        Identifier.fromNamespaceAndPath("superior_stonecutter", "load")
    );

    @WrapOperation(
        method = "executeTagFunctions",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/ServerFunctionManager;execute(Lnet/minecraft/commands/functions/CommandFunction;Lnet/minecraft/commands/CommandSourceStack;)V"
        )
    )
    private void mms$skipLoadedBanner(
        ServerFunctionManager manager,
        CommandFunction<CommandSourceStack> function,
        CommandSourceStack source,
        Operation<Void> original
    ) {
        if (!MMS$MUTED.contains(function.id())) {
            original.call(manager, function, source);
        }
    }
}
