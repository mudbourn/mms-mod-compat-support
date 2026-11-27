package info.mudbourn.mmscompat.client;

import info.mudbourn.mmscompat.client.sharedkeys.SharedKeys;
import io.github.leawind.perspectiveapi.api.PerspectiveAPI;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.lang.reflect.Method;

// Forces first person when there is no room behind the player, riding a dragon, or using Axiom, and swaps the camera's shoulder away from walls.
public final class CrampedFirstPerson {

    private static final String FIRST_PERSON = "perspective_api.first_person";
    private static final String THIRD_PERSON = "leawind_third_person.third_person";
    private static final int OVERRIDE_PRIORITY = 1000;
    private static final double PLAYER_RADIUS = 0.5;
    private static final double FLOOR_MARGIN = 0.05;
    private static final double CAMERA_DISTANCE = 4.0;
    private static final double ENTER_ROOM = 1.0;
    private static final double LEAVE_ROOM = 2.0;
    private static final int ENTER_TICKS = 5;
    private static final int LEAVE_TICKS = 20;
    private static final double MARCH_STEP = 0.1;
    private static final double SHOULDER_WIDTH = 1.0;
    private static final double SWAP_ROOM = 1.5;
    private static final double SWAP_MARGIN = 1.0;
    private static final double RESTORE_ROOM = 2.5;
    private static final int SWAP_COOLDOWN_TICKS = 40;

    private static volatile boolean cramped;
    private static volatile boolean axiomOpen;
    private static volatile boolean dragon;
    private static int squeezeTicks;
    private static int roomyTicks;
    private static int swapCooldown;
    private static double chosenSide;
    private static boolean swapped;
    private static boolean wasForced;
    private static float[] pending;

    private static Object look;
    private static Method lookInitialized;
    private static Method lookPitch;
    private static Method lookYaw;
    private static Method lookInitialize;

    private static final Method AXIOM_EDITOR_ACTIVE;
    private static final Method AXIOM_MENU_INSTANCE;
    private static final Method AXIOM_MENU_ACTIVE;

    static {
        Method editor = null;
        Method instance = null;
        Method menu = null;
        try {
            editor = Class.forName("com.moulberry.axiom.editor.EditorUI").getMethod("isActive");
            Class<?> manager = Class.forName("com.moulberry.axiom.ContextMenuManager");
            instance = manager.getMethod("getInstance");
            menu = manager.getMethod("isActive");
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
        AXIOM_EDITOR_ACTIVE = editor;
        AXIOM_MENU_INSTANCE = instance;
        AXIOM_MENU_ACTIVE = menu;
    }

    private CrampedFirstPerson() {
    }

    // Called from LookControllerCaptureMixin with Leawind's camera look controller.
    public static void captureLook(Object controller) {
        try {
            Class<?> type = controller.getClass();
            lookInitialized = type.getMethod("isInitialized");
            lookPitch = type.getMethod("pitchDegrees");
            lookYaw = type.getMethod("yawDegrees");
            lookInitialize = type.getMethod("initialize", float.class, float.class);
            look = controller;
        } catch (ReflectiveOperationException e) {
            look = null;
        }
    }

    // Leawind's camera direction as {pitch, yaw}, or null when it isn't tracking one.
    private static float[] cameraRotation() {
        if (look == null) return null;
        try {
            if (!(boolean) lookInitialized.invoke(look)) return null;
            return new float[]{(float) lookPitch.invoke(look), (float) lookYaw.invoke(look)};
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(CrampedFirstPerson::tick);
        PerspectiveAPI.runWhenReady("mms_compat", () -> PerspectiveAPI.getOverrideChain()
                .register(OVERRIDE_PRIORITY, () -> cramped || axiomOpen || dragon ? FIRST_PERSON : null));
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            restoreShoulder();
            cramped = false;
            axiomOpen = false;
            dragon = false;
            wasForced = false;
            return;
        }
        boolean thirdPerson = !wasForced && PerspectiveAPI.isCurrent(THIRD_PERSON);
        float[] camera = thirdPerson ? cameraRotation() : null;
        Vec3 eye = player.getEyePosition();
        Vec3 view = player.getViewVector(1.0F);
        boolean hugging = nearWall(client.level, player, eye, PLAYER_RADIUS);
        double behind = room(client.level, player, eye, view.scale(-CAMERA_DISTANCE));
        squeezeTicks = hugging || behind < ENTER_ROOM ? squeezeTicks + 1 : 0;
        roomyTicks = !hugging && behind >= LEAVE_ROOM ? roomyTicks + 1 : 0;
        boolean tight = cramped ? roomyTicks < LEAVE_TICKS : squeezeTicks >= ENTER_TICKS;
        boolean axiom = axiomOpen();
        boolean riding = SharedKeys.ridingDragon();
        boolean forced = tight || axiom || riding;
        if (thirdPerson && !forced) {
            swapShoulder(client, player, eye, view);
        } else {
            restoreShoulder();
        }
        if (forced && !wasForced && camera != null) {
            pending = camera;
            face(player, camera);
        } else if (forced && pending != null) {
            face(player, pending);
            pending = null;
        } else if (!forced && wasForced && look != null) {
            try {
                lookInitialize.invoke(look, player.getXRot(), player.getYRot());
            } catch (ReflectiveOperationException ignored) {
            }
        }
        if (!forced) pending = null;
        wasForced = forced;
        cramped = tight;
        axiomOpen = axiom;
        dragon = riding;
    }

    // Moves the camera off the player's chosen shoulder while a wall blocks it, and back once that side is clear.
    private static void swapShoulder(Minecraft client, LocalPlayer player, Vec3 eye, Vec3 view) {
        double offsetSide = ShoulderSwap.side();
        if (offsetSide == 0.0) return;
        if (swapped && offsetSide != -chosenSide) swapped = false;
        if (!swapped) chosenSide = offsetSide;
        if (swapCooldown > 0) {
            swapCooldown--;
            return;
        }
        Camera camera = client.gameRenderer.mainCamera();
        if (!camera.isDetached()) return;
        Vec3 right = new Vec3(-view.z, 0.0, view.x);
        if (right.lengthSqr() < 1.0E-4) return;
        right = right.normalize();
        double worldSide = Math.signum(camera.position().subtract(eye).dot(right));
        if (worldSide == 0.0) return;
        double chosenWorldSide = swapped ? -worldSide : worldSide;
        Vec3 back = view.scale(-CAMERA_DISTANCE);
        double chosen = room(client.level, player, eye, back.add(right.scale(chosenWorldSide * SHOULDER_WIDTH)));
        if (swapped) {
            if (chosen >= RESTORE_ROOM) restoreShoulder();
            return;
        }
        if (chosen >= SWAP_ROOM) return;
        double other = room(client.level, player, eye, back.add(right.scale(-chosenWorldSide * SHOULDER_WIDTH)));
        if (other < chosen + SWAP_MARGIN) return;
        ShoulderSwap.setSide(-chosenSide);
        swapped = true;
        swapCooldown = SWAP_COOLDOWN_TICKS;
    }

    // Puts the camera back on the shoulder the player chose.
    private static void restoreShoulder() {
        if (!swapped) return;
        ShoulderSwap.setSide(chosenSide);
        swapped = false;
        swapCooldown = SWAP_COOLDOWN_TICKS;
    }

    // How far a ray from a point travels along an offset before it meets a solid block.
    private static double room(ClientLevel level, LocalPlayer player, Vec3 from, Vec3 offset) {
        double length = offset.length();
        Vec3 step = offset.scale(MARCH_STEP / length);
        Vec3 point = from;
        for (double travelled = MARCH_STEP; travelled < length; travelled += MARCH_STEP) {
            point = point.add(step);
            if (solidAt(level, player, point)) return travelled;
        }
        return length;
    }

    // A point inside a solid block, ignoring leaves, fluids and the floor.
    private static boolean solidAt(ClientLevel level, LocalPlayer player, Vec3 point) {
        BlockPos pos = BlockPos.containing(point);
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(BlockTags.LEAVES)) return false;
        double floor = floor(player);
        VoxelShape shape = state.getCollisionShape(level, pos, CollisionContext.of(player));
        for (AABB box : shape.toAabbs()) {
            AABB placed = box.move(pos);
            if (placed.maxY > floor && placed.contains(point)) return true;
        }
        return false;
    }

    // Blocks topping out at or below this height count as floor: the feet normally, the eyes while crawling.
    private static double floor(LocalPlayer player) {
        return player.isVisuallyCrawling() ? player.getEyeY() : player.getY() + FLOOR_MARGIN;
    }

    // A solid block within a radius of a point, ignoring leaves, fluids and the floor.
    private static boolean nearWall(ClientLevel level, LocalPlayer player, Vec3 center, double radius) {
        double floor = floor(player);
        double reach = radius * radius;
        BlockPos min = BlockPos.containing(center.subtract(radius, radius, radius));
        BlockPos max = BlockPos.containing(center.add(radius, radius, radius));
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.is(BlockTags.LEAVES)) continue;
            VoxelShape shape = state.getCollisionShape(level, pos, CollisionContext.of(player));
            for (AABB box : shape.toAabbs()) {
                AABB placed = box.move(pos);
                if (placed.maxY <= floor) continue;
                if (distanceSq(placed, center) <= reach) return true;
            }
        }
        return false;
    }

    private static double distanceSq(AABB box, Vec3 point) {
        double dx = Math.max(Math.max(box.minX - point.x, 0.0), point.x - box.maxX);
        double dy = Math.max(Math.max(box.minY - point.y, 0.0), point.y - box.maxY);
        double dz = Math.max(Math.max(box.minZ - point.z, 0.0), point.z - box.maxZ);
        return dx * dx + dy * dy + dz * dz;
    }

    // Sets the rotation and the previous-tick rotation together, so the game has nothing to blend.
    private static void face(LocalPlayer player, float[] rotation) {
        player.setXRot(rotation[0]);
        player.setYRot(rotation[1]);
        player.xRotO = rotation[0];
        player.yRotO = rotation[1];
        player.setYHeadRot(rotation[1]);
        player.yHeadRotO = rotation[1];
    }

    private static boolean axiomOpen() {
        if (AXIOM_EDITOR_ACTIVE == null) return false;
        try {
            return (boolean) AXIOM_EDITOR_ACTIVE.invoke(null)
                    || (boolean) AXIOM_MENU_ACTIVE.invoke(AXIOM_MENU_INSTANCE.invoke(null));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }
}
