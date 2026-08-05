package info.mudbourn.mmscompat.client.etfnbt;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/**
 * {@code /mmsnbt} — client-only switches for the ETF NBT fast path.
 *
 * <pre>
 * /mmsnbt fast              report whether the fast path is on
 * /mmsnbt fast on|off       serve nbt() lookups from live state, or from the full save
 * /mmsnbt verify            report whether verification is on
 * /mmsnbt verify on|off     run both paths and log disagreements
 * </pre>
 *
 * <p>The intended session is: {@code /mmsnbt verify on}, play for a while doing the
 * things the rules key off — swap between bow, crossbow, shield, quarterstaff,
 * pickaxe, in both hands — watch the log stay quiet, then {@code /mmsnbt verify off}.
 * A disagreement names the path it happened on. See {@link NbtFastPath}.
 */
public final class NbtCommand {

    private NbtCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
                dispatcher.register(ClientCommandManager.literal("mmsnbt")
                        .then(ClientCommandManager.literal("fast")
                                .executes(ctx -> report(ctx.getSource(), "fast path", NbtTuning.fastPath))
                                .then(ClientCommandManager.literal("on")
                                        .executes(ctx -> setFast(ctx.getSource(), true)))
                                .then(ClientCommandManager.literal("off")
                                        .executes(ctx -> setFast(ctx.getSource(), false))))
                        .then(ClientCommandManager.literal("verify")
                                .executes(ctx -> report(ctx.getSource(), "verification", NbtTuning.verify))
                                .then(ClientCommandManager.literal("on")
                                        .executes(ctx -> setVerify(ctx.getSource(), true)))
                                .then(ClientCommandManager.literal("off")
                                        .executes(ctx -> setVerify(ctx.getSource(), false))))));
    }

    private static int report(FabricClientCommandSource source, String what, boolean on) {
        source.sendFeedback(Component.literal("NBT " + what + ": " + (on ? "on" : "off")));
        return 1;
    }

    private static int setFast(FabricClientCommandSource source, boolean on) {
        NbtTuning.fastPath = on;
        NbtTuning.save();
        source.sendFeedback(Component.literal("NBT fast path: " + (on ? "on" : "off")
                + (on ? "" : " — every nbt() rule now costs a full entity serialisation")));
        return 1;
    }

    private static int setVerify(FabricClientCommandSource source, boolean on) {
        NbtTuning.verify = on;
        NbtTuning.save();
        source.sendFeedback(Component.literal("NBT verification: " + (on ? "on" : "off")
                + (on ? " — both paths run, disagreements go to the log" : "")));
        return 1;
    }
}
