package info.mudbourn.mmscompat.client;

import io.github.leawind.perspectiveapi.api.PerspectiveAPI;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;

// Forces first person through PerspectiveAPI's override chain where third person aim breaks: tight spots, and while Axiom's editor or context menu is open, since its picks come from the eyes.
public final class CrampedFirstPerson {

    private static final String FIRST_PERSON = "perspective_api.first_person";
    private static final String THIRD_PERSON = "leawind_third_person.third_person";
    private static final int OVERRIDE_PRIORITY = 1000;
    private static final double ENTER_DISTANCE = 1.0;
    private static final double LEAVE_DISTANCE = 1.5;
    private static final int LEAVE_DELAY_TICKS = 10;
    private static final float LOOK_DOWN_PITCH = 45.0F;
    private static final double LOOK_DOWN_EXTRA = 1.5;
    // Third person camera pushed this close to the eyes by terrain behind you.
    private static final double CAMERA_SQUEEZE = 1.2;
    // To leave, the space behind you along the view must be clear this far.
    private static final double CAMERA_CLEAR = 2.0;

    private static volatile boolean cramped;
    private static volatile boolean axiomOpen;
    private static int clearTicks;
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
            // Axiom isn't installed.
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
                .register(OVERRIDE_PRIORITY, () -> cramped || axiomOpen ? FIRST_PERSON : null));
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            cramped = false;
            axiomOpen = false;
            wasForced = false;
            return;
        }
        float[] camera = !wasForced && PerspectiveAPI.isCurrent(THIRD_PERSON) ? cameraRotation() : null;
        boolean axiom = axiomOpen();
        boolean tight;
        if (!cramped) {
            tight = tight(client, player, ENTER_DISTANCE, camera) || (camera != null && squeezed(client, player, camera));
            if (tight) clearTicks = 0;
        } else {
            clearTicks = tight(client, player, LEAVE_DISTANCE, null) || blockedBehind(client, player) ? 0 : clearTicks + 1;
            tight = clearTicks < LEAVE_DELAY_TICKS;
        }
        boolean forced = tight || axiom;
        if (forced && !wasForced && camera != null) {
            // Turn straight to the third person camera's direction.
            pending = camera;
            face(player, camera);
        } else if (forced && pending != null) {
            // Leawind can still turn the body on the switch frame, so set it once more.
            face(player, pending);
            pending = null;
        } else if (!forced && wasForced && look != null) {
            // Third person picks up from where you looked in first person.
            try {
                lookInitialize.invoke(look, player.getXRot(), player.getYRot());
            } catch (ReflectiveOperationException ignored) {
            }
        }
        if (!forced) pending = null;
        wasForced = forced;
        cramped = tight;
        axiomOpen = axiom;
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
            return (boolean) AXIOM_EDITOR_ACTIVE.invoke(null) || (boolean) AXIOM_MENU_ACTIVE.invoke(AXIOM_MENU_INSTANCE.invoke(null));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    // Terrain right behind the player along the third person camera's view, which would push the camera in next to
    // the player (e.g. climbing a hillside). A ray, not the camera's position: the camera starts at the eyes when third
    // person comes back and eases out, so its position would read as squeezed every time and loop.
    private static boolean squeezed(Minecraft client, LocalPlayer player, float[] camera) {
        return behind(client, player, Vec3.directionFromRotation(camera[0], camera[1]), CAMERA_SQUEEZE);
    }

    // Terrain behind the player along the view, where the third person camera would sit.
    private static boolean blockedBehind(Minecraft client, LocalPlayer player) {
        return behind(client, player, player.getViewVector(1.0F), CAMERA_CLEAR);
    }

    private static boolean behind(Minecraft client, LocalPlayer player, Vec3 look, double distance) {
        Vec3 eye = player.getEyePosition();
        Vec3 back = eye.subtract(look.scale(distance));
        return client.level.clip(new ClipContext(eye, back, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.BLOCK;
    }

    // Tight means a wall right in front, climbing a ladder or vine, or looking down at a nearby door, trapdoor or ladder.
    private static boolean tight(Minecraft client, LocalPlayer player, double distance, float[] camera) {
        if (player.onClimbable()) return true;
        Vec3 eye = player.getEyePosition();
        Vec3 look = camera != null ? Vec3.directionFromRotation(camera[0], camera[1]) : player.getViewVector(1.0F);
        float pitch = camera != null ? camera[0] : player.getXRot();
        // A wall only counts while looking roughly level; looking down at a block by your feet isn't cramped.
        if (Math.abs(pitch) < LOOK_DOWN_PITCH && client.level.clip(new ClipContext(eye, eye.add(look.scale(distance)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.BLOCK) return true;
        if (pitch < LOOK_DOWN_PITCH) return false;
        BlockHitResult below = client.level.clip(new ClipContext(eye, eye.add(look.scale(distance + LOOK_DOWN_EXTRA)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (below.getType() != HitResult.Type.BLOCK) return false;
        BlockState state = client.level.getBlockState(below.getBlockPos());
        return state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock || state.is(BlockTags.CLIMBABLE);
    }
}
