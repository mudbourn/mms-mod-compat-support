package info.mudbourn.mmscompat.bubble;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Spectating sessions that let a player look through a surveillance camera. */
public final class CameraView {

    private static final AttachmentType<ReturnPoint> RETURN = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath("mms_compat", "camera_return"),
        builder -> builder.persistent(ReturnPoint.CODEC)
    );

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private static final float VIEW_PITCH = 20.0f;

    private static final int JUMP_COOLDOWN_TICKS = 5;

    private static final int HINT_INTERVAL_TICKS = 20;

    private static final int CAMERA_RESEND_TICKS = 100;

    private static final int CAMERA_RESEND_INTERVAL = 5;

    private CameraView() {}

    private static final class Session {
        final CameraViewEntity entity;
        final ServerLevel level;
        final BlockPos camera;
        boolean jumpHeld;
        int jumpCooldown;
        int age;

        Session(CameraViewEntity entity, ServerLevel level, BlockPos camera) {
            this.entity = entity;
            this.level = level;
            this.camera = camera;
        }
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CameraView::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!SESSIONS.containsKey(handler.getPlayer().getUUID())) {
                restore(handler.getPlayer(), true);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> abandon(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
                abandon(player);
            }
            SESSIONS.clear();
        });
    }

    public static void startFromMonitor(ServerPlayer player, SecurityMonitorBlockEntity monitor) {
        CameraLink link = monitor.getLink();
        if (link == null) {
            fail(player, "ui.bubblellaneous.camera.offline");
            return;
        }
        MinecraftServer server = player.level().getServer();
        ServerLevel target = server.getLevel(link.pos().dimension());
        BlockPos found = target == null ? null : resolve(target, link);
        if (found == null) {
            fail(player, "ui.bubblellaneous.camera.not_found");
            return;
        }
        if (!found.equals(link.pos().pos())) {
            monitor.setLink(new CameraLink(GlobalPos.of(link.pos().dimension(), found), link.name(), false));
        }
        start(player, target, found);
    }

    private static BlockPos resolve(ServerLevel level, CameraLink link) {
        BlockPos pos = link.pos().pos();
        if (isCamera(level, pos)) {
            return pos;
        }
        if (link.legacy()) {
            for (int dx = -1; dx <= 0; dx++) {
                for (int dz = -1; dz <= 0; dz++) {
                    BlockPos candidate = pos.offset(dx, 0, dz);
                    if (isCamera(level, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isCamera(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SurveillanceCameraBlockEntity;
    }

    private static void start(ServerPlayer player, ServerLevel level, BlockPos cameraPos) {
        if (SESSIONS.containsKey(player.getUUID())) {
            return;
        }
        SurveillanceCameraBlockEntity camera = (SurveillanceCameraBlockEntity) level.getBlockEntity(cameraPos);
        if (player.getAttached(RETURN) == null) {
            player.setAttached(
                RETURN,
                new ReturnPoint(
                    player.level().dimension(),
                    player.position(),
                    player.getYRot(),
                    player.getXRot(),
                    player.gameMode()
                )
            );
        }
        CameraViewEntity entity = new CameraViewEntity(BubbleContent.CAMERA_VIEW, level);
        place(entity, camera);
        level.addFreshEntity(entity);
        Session session = new Session(entity, level, cameraPos);
        SESSIONS.put(player.getUUID(), session);
        player.setGameMode(GameType.SPECTATOR);
        player.teleportTo(
            level,
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            Set.<Relative>of(),
            entity.getYRot(),
            entity.getXRot(),
            true
        );
        player.setCamera(entity);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, true, false));
        cue(player, SoundEvents.HORSE_ARMOR.value(), SoundSource.BLOCKS, 0.5f);
        hint(player, camera);
    }

    private static void place(CameraViewEntity entity, SurveillanceCameraBlockEntity camera) {
        Vec3 head = camera.headPos();
        entity.setPos(head.x, head.y - entity.getEyeHeight(), head.z);
        entity.setYRot(camera.viewYaw());
        entity.setXRot(VIEW_PITCH);
        entity.setCamera(camera.getBlockPos());
    }

    private static void tick(MinecraftServer server) {
        for (UUID id : new ArrayList<>(SESSIONS.keySet())) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            Session session = SESSIONS.get(id);
            if (player == null) {
                SESSIONS.remove(id);
                session.entity.discard();
                continue;
            }
            if (!player.isSpectator()) {
                finish(player, session, false, null);
            } else if (player.getCamera() != session.entity || session.entity.isRemoved()) {
                finish(player, session, true, null);
            } else if (
                !(session.level.getBlockEntity(session.camera) instanceof SurveillanceCameraBlockEntity camera)
            ) {
                finish(player, session, true, "ui.bubblellaneous.camera.not_found");
            } else {
                drive(player, session, camera);
            }
        }
    }

    private static void drive(ServerPlayer player, Session session, SurveillanceCameraBlockEntity camera) {
        Input input = player.getLastClientInput();
        if (input.left() != input.right()) {
            if (!camera.isManual()) {
                camera.setManual(true);
                cue(player, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1.0f);
                hint(player, camera);
            }
            camera.turn(input.right() ? 1 : -1);
        }
        if (session.jumpCooldown > 0) {
            session.jumpCooldown--;
        }
        if (input.jump() && !session.jumpHeld && session.jumpCooldown == 0) {
            camera.setManual(!camera.isManual());
            session.jumpCooldown = JUMP_COOLDOWN_TICKS;
            cue(player, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1.0f);
            hint(player, camera);
        }
        session.jumpHeld = input.jump();
        camera.advance(session.level);
        session.age++;
        if (session.age <= CAMERA_RESEND_TICKS && session.age % CAMERA_RESEND_INTERVAL == 0) {
            player.connection.send(new ClientboundSetCameraPacket(session.entity));
        }
        if (session.age % HINT_INTERVAL_TICKS == 0) {
            hint(player, camera);
        }
    }

    private static void hint(ServerPlayer player, SurveillanceCameraBlockEntity camera) {
        Component left = Component.keybind("key.left").withStyle(ChatFormatting.BOLD);
        Component right = Component.keybind("key.right").withStyle(ChatFormatting.BOLD);
        Component text = camera.isManual()
            ? Component.translatable(
                "ui.bubblellaneous.camera.manual_mode",
                left,
                right,
                Component.keybind("key.jump").withStyle(ChatFormatting.BOLD)
            )
            : Component.translatable(
                "mms_compat.camera.auto_mode",
                left,
                right,
                Component.keybind("key.sneak").withStyle(ChatFormatting.BOLD)
            );
        player.displayClientMessage(text.copy().withStyle(style -> style.withColor(0xFCD78D)), true);
    }

    private static void finish(ServerPlayer player, Session session, boolean restoreMode, String failureKey) {
        SESSIONS.remove(player.getUUID());
        session.entity.discard();
        resetManual(session);
        restore(player, restoreMode);
        if (failureKey != null) {
            fail(player, failureKey);
        }
    }

    private static void resetManual(Session session) {
        if (
            session.level.getBlockEntity(session.camera) instanceof SurveillanceCameraBlockEntity camera
                && camera.isManual()
        ) {
            camera.setManual(false);
        }
    }

    private static void abandon(ServerPlayer player) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session != null) {
            session.entity.discard();
            resetManual(session);
            player.setCamera(player);
        }
    }

    private static void restore(ServerPlayer player, boolean restoreMode) {
        ReturnPoint point = player.getAttached(RETURN);
        if (point == null) {
            return;
        }
        player.removeAttached(RETURN);
        player.setCamera(player);
        ServerLevel dimension = player.level().getServer().getLevel(point.dimension());
        if (dimension != null) {
            player.teleportTo(
                dimension,
                point.pos().x,
                point.pos().y,
                point.pos().z,
                Set.<Relative>of(),
                point.yaw(),
                point.pitch(),
                true
            );
        }
        if (restoreMode) {
            player.setGameMode(point.gameType());
        }
    }

    private static void cue(ServerPlayer player, SoundEvent sound, SoundSource source, float volume) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, source, volume, 1.0f);
    }

    private static void fail(ServerPlayer player, String key) {
        cue(player, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 0.5f);
        player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
    }
}
