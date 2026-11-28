package info.mudbourn.mmscompat.client;

import info.mudbourn.mmscompat.client.sharedkeys.SharedKeys;
import io.github.leawind.perspectiveapi.api.PerspectiveAPI;
import io.github.leawind.perspectiveapi.internal.impl.TransitionImpl;
import io.github.leawind.perspectiveapi.internal.impl.transition.rotation.RotationTransitionAlgorithm;
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
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.lang.reflect.Method;

// Forces first person when a block is close beside, above or behind the player, the player looks steeply up or down, the camera's shoulder is against a wall, the player rides a dragon, or uses Axiom.
public final class CrampedFirstPerson {

    private static final String FIRST_PERSON = "perspective_api.first_person";
    private static final String THIRD_PERSON = "leawind_third_person.third_person";
    private static final int OVERRIDE_PRIORITY = 1000;
    private static final double SIDE_ROOM = 2.0;
    private static final float ENTER_PITCH = 65.0F;
    private static final float LEAVE_PITCH = 60.0F;
    private static final double FLOOR_MARGIN = 0.05;
    private static final double CAMERA_DISTANCE = 4.0;
    private static final double ENTER_ROOM = 1.0;
    private static final double LEAVE_ROOM = 2.0;
    private static final int ENTER_TICKS = 1;
    private static final int LEAVE_TICKS = 20;
    private static final double MARCH_STEP = 0.1;
    private static final double SHOULDER_WIDTH = 1.0;
    private static final double SHOULDER_ENTER_ROOM = 1.5;
    private static final double SHOULDER_LEAVE_ROOM = 2.5;
    private static final int FORCED_TRANSITION_TICKS = 4;
    private static final RotationTransitionAlgorithm SNAP_ROTATION = new RotationTransitionAlgorithm() {
        @Override
        public String id() {
            return "mms_compat:snap";
        }

        @Override
        public void start(Quaternionfc rotation) {
        }

        @Override
        public void update(float progress, Quaternionfc target, Quaternionf out) {
            out.set(target);
        }
    };

    private static volatile boolean cramped;
    private static volatile boolean axiomOpen;
    private static volatile boolean dragon;
    private static int squeezeTicks;
    private static int roomyTicks;
    private static double shoulderSign;
    private static boolean wasForced;
    private static float[] pending;
    private static int fastTicks;
    private static double savedDurationMs;
    private static RotationTransitionAlgorithm savedRotation;

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
            if (fastTicks > 0) {
                fastTicks = 0;
                restoreTransition();
            }
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
        boolean boxed = boxedIn(client.level, player, eye);
        double shoulder = shoulderRoom(client, player, eye, view);
        double behind = room(client.level, player, eye, view.scale(-CAMERA_DISTANCE));
        float pitch = Math.abs(camera != null ? camera[0] : player.getXRot());
        squeezeTicks = boxed || behind < ENTER_ROOM || shoulder < SHOULDER_ENTER_ROOM || pitch >= ENTER_PITCH ? squeezeTicks + 1 : 0;
        roomyTicks = !boxed && behind >= LEAVE_ROOM && shoulder >= SHOULDER_LEAVE_ROOM && pitch < LEAVE_PITCH ? roomyTicks + 1 : 0;
        boolean tight = cramped ? roomyTicks < LEAVE_TICKS : squeezeTicks >= ENTER_TICKS;
        boolean axiom = axiomOpen();
        boolean riding = SharedKeys.ridingDragon();
        boolean forced = tight || axiom || riding;
        if (forced && !wasForced) {
            fastTransition();
        } else if (fastTicks > 0 && --fastTicks == 0) {
            restoreTransition();
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

    // Skips Perspective API's transition and snaps its rotation for the switch into forced first person.
    private static void fastTransition() {
        if (!(PerspectiveAPI.getTransition() instanceof TransitionImpl transition)) return;
        if (fastTicks == 0) {
            savedDurationMs = transition.getDurationMs();
            savedRotation = transition.getRotationAlgorithm();
        }
        transition.setDurationMs(0.0);
        transition.setRotationAlgorithm(SNAP_ROTATION);
        fastTicks = FORCED_TRANSITION_TICKS;
    }

    // Puts back the transition length and rotation blending the forced switch replaced.
    private static void restoreTransition() {
        if (!(PerspectiveAPI.getTransition() instanceof TransitionImpl transition)) return;
        transition.setDurationMs(savedDurationMs);
        transition.setRotationAlgorithm(savedRotation);
    }

    // How far the camera's shoulder spot behind the player is from a wall, or the full distance while centered.
    private static double shoulderRoom(Minecraft client, LocalPlayer player, Vec3 eye, Vec3 view) {
        double offsetSide = CameraShoulder.side();
        Vec3 right = new Vec3(-view.z, 0.0, view.x);
        if (offsetSide == 0.0 || right.lengthSqr() < 1.0E-4) return CAMERA_DISTANCE;
        right = right.normalize();
        Camera camera = client.gameRenderer.mainCamera();
        if (camera.isDetached()) {
            double worldSide = Math.signum(camera.position().subtract(eye).dot(right));
            if (worldSide != 0.0) shoulderSign = worldSide * offsetSide;
        }
        if (shoulderSign == 0.0) return CAMERA_DISTANCE;
        Vec3 spot = view.scale(-CAMERA_DISTANCE).add(right.scale(shoulderSign * offsetSide * SHOULDER_WIDTH));
        return room(client.level, player, eye, spot);
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

    // A solid block within reach to the player's left, right or above their eyes.
    private static boolean boxedIn(ClientLevel level, LocalPlayer player, Vec3 eye) {
        Vec3 right = Vec3.directionFromRotation(0.0F, player.getYRot() + 90.0F).scale(SIDE_ROOM);
        return room(level, player, eye, right) < SIDE_ROOM
                || room(level, player, eye, right.scale(-1.0)) < SIDE_ROOM
                || room(level, player, eye, new Vec3(0.0, SIDE_ROOM, 0.0)) < SIDE_ROOM;
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
