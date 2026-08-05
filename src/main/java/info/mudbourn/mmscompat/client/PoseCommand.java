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
                                        .executes(ctx -> set(ctx.getSource(), true))))
                        .then(ClientCommandManager.literal("ease")
                                .executes(ctx -> reportEase(ctx.getSource()))
                                .then(ClientCommandManager.argument("milliseconds",
                                                com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 2000))
                                        .executes(ctx -> setEase(ctx.getSource(),
                                                com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "milliseconds")))))
                        .then(ClientCommandManager.literal("debug")
                                .executes(ctx -> reportDebug(ctx.getSource()))
                                .then(ClientCommandManager.literal("on")
                                        .executes(ctx -> setDebug(ctx.getSource(), true)))
                                .then(ClientCommandManager.literal("off")
                                        .executes(ctx -> setDebug(ctx.getSource(), false))))));
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

    private static int reportEase(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal(
                "Arm handover ease: " + PoseTuning.transitionEaseMs + "ms"
                        + (PoseTuning.transitionEaseMs == 0 ? " (hard cut)" : "")));
        return 1;
    }

    private static int setEase(FabricClientCommandSource source, int milliseconds) {
        PoseTuning.transitionEaseMs = milliseconds;
        PoseTuning.save();
        source.sendFeedback(Component.literal(
                "Arm handover ease: " + milliseconds + "ms (takes effect immediately)."));
        return 1;
    }

    private static int reportDebug(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal(
                "Pose trace: " + (PoseDebug.enabled ? "on" : "off")));
        return 1;
    }

    /**
     * Deliberately not written to {@code mms_compat_pose.json}. The trace is a
     * measurement, not a preference — leaving it on across a restart would quietly
     * fill the log of a session nobody is debugging.
     */
    private static int setDebug(FabricClientCommandSource source, boolean on) {
        PoseDebug.enabled = on;
        source.sendFeedback(Component.literal(on
                ? "Pose trace: on — one report a second for your own player, to the log."
                : "Pose trace: off."));
        return 1;
    }
}
