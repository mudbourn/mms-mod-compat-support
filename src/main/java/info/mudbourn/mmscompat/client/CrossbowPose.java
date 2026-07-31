package info.mudbourn.mmscompat.client;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import strm.emfcompat.core.PoseManager;
import strm.emfcompat.core.PoseSnapshot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Owns both arms for the whole time a crossbow is held.
 *
 * <h2>Why this is not a pack edit</h2>
 *
 * <p>DetailedAnimations models a bow as one boolean and two timers:
 * {@code BowShootingRightPose} is {@code is_using_item && <bow id>}, its
 * {@code StartTimer} runs the raise, and its {@code EndTimer} — which starts the
 * moment use <em>ends</em> — runs the release. That is correct for a bow, where
 * use is the draw and the end of use is the shot, and wrong for a crossbow in
 * both halves: use is <em>loading</em>, and the end of loading is not a shot. So
 * a crossbow charged up and then played a firing animation.
 *
 * <p>It cannot be repaired in the jem. A crossbow needs a shot <em>event</em>, and
 * CEM expressions are pure functions of current state — no events, and no charge
 * progress to key on either. Crossbows were therefore removed from DA's bow regex
 * and the whole sequence is authored here, over {@link PoseManager}, which beats
 * the CEM animation by construction (see {@code EMFModelPartRootMixin}).
 *
 * <h2>States</h2>
 *
 * <p>Hold, load and fire, and one of them is always applied while a crossbow is in
 * either hand — loaded or not, in use or not. The continuity is the point: an arm
 * that is never unposed can never fall back to DA's idle, which is what dropped
 * the crossbow to the hip between shots.
 *
 * <h2>Detecting the shot</h2>
 *
 * <p>By watching {@link DataComponents#CHARGED_PROJECTILES} shrink, not by watching
 * input. Input is the wrong signal three times over: the vanilla crossbow fires on
 * right-click and {@code weaponsexpanded:chain_crossbow} on left-click (its
 * {@code MinecraftClientChainCrossbowAttackMixin}), the chain crossbow fires four
 * times per load, and above all input for <em>other</em> players is not observable
 * on this client at all — the ammo count is, because it rides along in the synced
 * stack. Watching ammo gets all three for free.
 */
public final class CrossbowPose {

    /** Distinct from {@code mms_held_pose}; {@code PoseManager} merges per part, per source. */
    public static final String SOURCE = "mms_crossbow";

    /** Length of the recoil, in ticks. */
    private static final float FIRE_TICKS = 6.0f;

    private static final Map<UUID, State> STATES = new HashMap<>();

    private CrossbowPose() {}

    private static final class State {
        long lastGameTime = Long.MIN_VALUE;
        ItemStack lastStack = ItemStack.EMPTY;
        int lastCharged;
        long fireStart = Long.MIN_VALUE;
    }

    /**
     * @return true if this class posed the arms, meaning no other producer should.
     */
    public static boolean apply(AbstractClientPlayer player, PlayerModel model) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        boolean inMain = main.getItem() instanceof CrossbowItem;
        boolean inOff = !inMain && off.getItem() instanceof CrossbowItem;
        if (!inMain && !inOff) {
            release(player.getUUID());
            return false;
        }

        ItemStack stack = inMain ? main : off;
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        long gameTime = player.level().getGameTime();
        advance(state, stack, gameTime);

        // The weapon arm is the one actually holding it; the other one supports the
        // stock and works the crank.
        boolean weaponIsRight = (player.getMainArm() == HumanoidArm.RIGHT) == inMain;
        // Yaw sign, not arm choice — the arms are picked below off weaponIsRight.
        // A raised arm's yRot swings it toward the player's left when positive, so
        // the *right* arm needs negative yaw to come inward. Both arms have to
        // converge on the stock; matching signs splay them into a T-pose. Vanilla's
        // animateCrossbowHold uses right -0.3 / left +0.6 for the same reason.
        float side = weaponIsRight ? -1.0f : 1.0f;

        float weaponX = -1.25f;
        float weaponY = side * 0.30f;
        float weaponZ = side * 0.05f;
        float supportX = -1.30f;
        float supportY = -side * 0.55f;
        float supportZ = -side * 0.20f;

        // Loading: the support hand travels back along the stock and returns, once,
        // over the real charge time rather than over a fixed animation length.
        if (player.isUsingItem() && player.getUseItem() == stack) {
            float crank = (float) Math.sin(Math.PI * loadProgress(player, stack));
            weaponX += 0.35f * crank;
            supportX += 0.55f * crank;
            supportY = -side * (0.55f - 0.25f * crank);
        }

        // Firing: an impulse that decays, laid over whichever pose is current.
        float kick = kick(state, player, gameTime);
        if (kick > 0.0f) {
            weaponX -= 0.30f * kick;
            weaponZ += side * 0.08f * kick;
            supportX -= 0.20f * kick;
        }

        var weaponArm = weaponIsRight ? model.rightArm : model.leftArm;
        var supportArm = weaponIsRight ? model.leftArm : model.rightArm;

        // Author onto the live parts, snapshot, then put them back: the snapshot is
        // re-applied after the CEM animation anyway, and leaving the model mutated
        // here would be a side effect on anything that reads it in between.
        PoseSnapshot weaponBefore = new PoseSnapshot(weaponArm);
        PoseSnapshot supportBefore = new PoseSnapshot(supportArm);

        weaponArm.xRot = weaponX;
        weaponArm.yRot = weaponY;
        weaponArm.zRot = weaponZ;
        supportArm.xRot = supportX;
        supportArm.yRot = supportY;
        supportArm.zRot = supportZ;

        PoseSnapshot weaponPose = new PoseSnapshot(weaponArm);
        PoseSnapshot supportPose = new PoseSnapshot(supportArm);

        weaponBefore.apply(weaponArm);
        supportBefore.apply(supportArm);

        PoseSnapshot left = weaponIsRight ? supportPose : weaponPose;
        PoseSnapshot right = weaponIsRight ? weaponPose : supportPose;
        PoseManager.savePoses(player.getUUID(), SOURCE, left, right);
        return true;
    }

    /** Drops the pose and the tracking, in the same frame the crossbow leaves the hand. */
    public static void release(UUID id) {
        if (STATES.remove(id) != null) {
            PoseManager.clearPoses(id, SOURCE);
        }
    }

    /** Ammo bookkeeping, once per tick rather than once per frame. */
    private static void advance(State state, ItemStack stack, long gameTime) {
        if (gameTime == state.lastGameTime) {
            return;
        }

        int charged = chargedCount(stack);
        // Only a drop on the *same* stack is a shot. Swapping a loaded crossbow for
        // an empty one also drops the count, and is not.
        boolean sameStack = ItemStack.isSameItemSameComponents(state.lastStack, stack)
                || (!state.lastStack.isEmpty() && state.lastStack.getItem() == stack.getItem());
        if (state.lastGameTime != Long.MIN_VALUE && sameStack && charged < state.lastCharged) {
            state.fireStart = gameTime;
        }

        state.lastCharged = charged;
        state.lastStack = stack.copy();
        state.lastGameTime = gameTime;
        prune(gameTime);
    }

    private static int chargedCount(ItemStack stack) {
        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        return charged == null ? 0 : charged.getItems().size();
    }

    private static float loadProgress(AbstractClientPlayer player, ItemStack stack) {
        int duration = CrossbowItem.getChargeDuration(stack, player);
        if (duration <= 0) {
            return 1.0f;
        }
        float used = stack.getUseDuration(player) - player.getUseItemRemainingTicks();
        return Math.clamp(used / duration, 0.0f, 1.0f);
    }

    private static float kick(State state, AbstractClientPlayer player, long gameTime) {
        if (state.fireStart == Long.MIN_VALUE) {
            return 0.0f;
        }
        float partial = net.minecraft.client.Minecraft.getInstance()
                .getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float elapsed = (gameTime - state.fireStart) + partial;
        if (elapsed >= FIRE_TICKS) {
            state.fireStart = Long.MIN_VALUE;
            return 0.0f;
        }
        return 1.0f - Math.clamp(elapsed / FIRE_TICKS, 0.0f, 1.0f);
    }

    /** Players who stop being rendered never come back through {@link #release}. */
    private static void prune(long gameTime) {
        Iterator<Map.Entry<UUID, State>> it = STATES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, State> entry = it.next();
            if (gameTime - entry.getValue().lastGameTime > 200L) {
                PoseManager.clearPoses(entry.getKey(), SOURCE);
                it.remove();
            }
        }
    }
}
