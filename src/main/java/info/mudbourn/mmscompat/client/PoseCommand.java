package info.mudbourn.mmscompat.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/**
 * {@code /mmspose} — a client-only command for switching held-pose arm modes
 * without a rebuild.
 *
 * <pre>
 * /mmspose arms              report the current mode
 * /mmspose arms absolute     hold replaces DA's arms (default)
 * /mmspose arms additive     hold is added on top of DA's arms
 * </pre>
 *
 * <p>The point is comparing the two back to back in one session. The arm modes
 * were previously swapped by editing code and rebuilding, which meant every
 * comparison spanned a restart and a version bump, and three of them in a row
 * produced no usable answer. Switching takes effect on the next rendered frame;
 * the choice is written to {@code config/mms_compat_pose.json} so it survives a
 * restart. See {@link PoseTuning} for what the modes actually do.
 */
public final class PoseCommand {

    private PoseCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
                dispatcher.register(ClientCommandManager.literal("mmspose")
                        .then(ClientCommandManager.literal("arms")
                                .executes(ctx -> report(ctx.getSource()))
                                .then(ClientCommandManager.literal("absolute")
                                        .executes(ctx -> set(ctx.getSource(), false)))
                                .then(ClientCommandManager.literal("additive")
                                        .executes(ctx -> set(ctx.getSource(), true))))));
    }

    private static int report(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal(
                "Held-pose arm mode: " + mode(PoseTuning.additiveArms)));
        return 1;
    }

    private static int set(FabricClientCommandSource source, boolean additive) {
        if (PoseTuning.additiveArms == additive) {
            source.sendFeedback(Component.literal(
                    "Held-pose arm mode is already " + mode(additive) + "."));
            return 0;
        }
        PoseTuning.additiveArms = additive;
        PoseTuning.save();
        source.sendFeedback(Component.literal(
                "Held-pose arm mode: " + mode(additive) + " (takes effect immediately)."));
        return 1;
    }

    private static String mode(boolean additive) {
        return additive ? "additive" : "absolute";
    }
}
