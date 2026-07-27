package info.mudbourn.mmscompat.mixin.metrofix;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.example.modmetro.MetroCartEntity;
import com.example.modmetro.MetroMod;
import com.example.modmetro.MetroSpawnerItem;
import com.example.modmetro.config.MetroConfig;

import info.mudbourn.mmscompat.metro.MetroRailPath;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;

/**
 * Rail-validated cart placement for the spawner item.
 *
 * ModMetro's own {@code useOn} checks for a rail under the clicked block
 * only, then places carts along the raw player look vector at
 * {@code spacing} intervals with zero further rail validation: off-rail,
 * diagonal, into walls, or past the end of track are all silently allowed.
 * It also lifts to {@code y + 0.5} instead of the correct on-rail height.
 *
 * This mixin replaces the whole method (cancellable @Inject at HEAD): it
 * walks the real rail spine — {@link MetroRailPath}, orthogonal adjacency
 * only, same BFS the rest of round 1/2 relies on — behind the clicked rail
 * first (matching the old look-vector-behind layout), then forward if that
 * doesn't fit the full consist, and only spawns if one direction holds every
 * car. Otherwise it spawns nothing, leaves the item in hand, and tells the
 * player how many cars actually fit.
 *
 * Car-to-car spacing is expressed in rail blocks along the spine, using the
 * same {@code max(1, round(spacing))} convention {@code
 * MetroFollowerSeparationMixin} already uses to walk the spine in rail-block
 * units.
 */
@Mixin(MetroSpawnerItem.class)
public abstract class MetroSpawnValidationMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$validateSpawn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level world = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (!(world.getBlockState(clicked).getBlock() instanceof BaseRailBlock)) {
            return; // not on a rail: let ModMetro's own PASS happen
        }
        cir.setReturnValue(mmsCompat$doSpawn(world, clicked, context));
    }

    @org.spongepowered.asm.mixin.Unique
    private InteractionResult mmsCompat$doSpawn(Level world, BlockPos clicked, UseOnContext context) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        int numCars = MetroConfig.wagons;
        double spacing = MetroConfig.spacing;
        int step = Math.max(1, (int) Math.round(spacing));
        Player player = context.getPlayer();

        double lookX = 1.0;
        double lookZ = 0.0;
        if (player != null) {
            float yaw = player.getYRot();
            lookX = -Math.sin(yaw * Math.PI / 180.0);
            lookZ = Math.cos(yaw * Math.PI / 180.0);
        }

        int maxSteps = (numCars * step) + 16;

        // Old layout put the lead cart at the clicked block and the rest
        // behind it (against the look vector); try that first.
        List<BlockPos> spine = mmsCompat$findSpine(world, clicked, -lookX, -lookZ, numCars, step, maxSteps);
        if (spine == null) {
            spine = mmsCompat$findSpine(world, clicked, lookX, lookZ, numCars, step, maxSteps);
        }

        if (spine == null) {
            int fit = Math.max(
                    mmsCompat$maxFit(world, clicked, -lookX, -lookZ, step, maxSteps),
                    mmsCompat$maxFit(world, clicked, lookX, lookZ, step, maxSteps));
            if (player != null) {
                player.displayClientMessage(Component.translatable(
                        "mms_compat.metro.cmd.spawn_insufficient_track", fit, numCars), true);
            }
            return InteractionResult.FAIL;
        }

        UUID leadUuid = null;
        for (int i = 0; i < numCars; i++) {
            BlockPos rail = spine.get(i);
            MetroCartEntity cart = new MetroCartEntity(MetroMod.METRO_CART, world);
            cart.setPos(rail.getX() + 0.5, rail.getY() + 0.0625, rail.getZ() + 0.5);
            if (i == 0) {
                leadUuid = cart.getUUID();
            }
            cart.setTrainData(leadUuid, i);
            world.addFreshEntity(cart);
        }

        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    /**
     * The {@code numCars} rail positions the consist would occupy walking
     * away from {@code clicked} towards {@code (dirX, dirZ)}, spaced
     * {@code step} rail blocks apart, or null if the spine does not reach
     * far enough (no rail neighbour that way, a dead end, or the direction
     * is not actually rail-connected).
     */
    @org.spongepowered.asm.mixin.Unique
    private List<BlockPos> mmsCompat$findSpine(Level world, BlockPos clicked, double dirX, double dirZ,
                                                int numCars, int step, int maxSteps) {
        if (numCars <= 1) {
            return List.of(clicked);
        }
        BlockPos neighbor = mmsCompat$neighborTowards(world, clicked, dirX, dirZ);
        if (neighbor == null) {
            return null;
        }
        int needed = (numCars - 1) * step - 1; // spineBehind already yields [clicked, neighbor, ...]
        List<BlockPos> spine = MetroRailPath.spineBehind(world, neighbor, clicked, maxSteps, Math.max(0, needed));
        int required = (numCars - 1) * step + 1;
        if (spine == null || spine.size() < required) {
            return null;
        }
        List<BlockPos> chosen = new ArrayList<>(numCars);
        for (int i = 0; i < numCars; i++) {
            chosen.add(spine.get(i * step));
        }
        return chosen;
    }

    /** How many cars, spaced {@code step} rail blocks apart, fit walking towards {@code (dirX, dirZ)}. */
    @org.spongepowered.asm.mixin.Unique
    private int mmsCompat$maxFit(Level world, BlockPos clicked, double dirX, double dirZ, int step, int maxSteps) {
        BlockPos neighbor = mmsCompat$neighborTowards(world, clicked, dirX, dirZ);
        if (neighbor == null) {
            return 1; // just the clicked rail itself
        }
        List<BlockPos> spine = MetroRailPath.spineBehind(world, neighbor, clicked, maxSteps, maxSteps);
        if (spine == null) {
            return 1;
        }
        return (spine.size() - 1) / step + 1;
    }

    /**
     * The rail-connected orthogonal neighbour of {@code from} most aligned
     * with {@code (dirX, dirZ)}, or null if none of its rail neighbours lean
     * that way at all (dead end, or the only connection continues in the
     * opposite direction).
     */
    @org.spongepowered.asm.mixin.Unique
    private BlockPos mmsCompat$neighborTowards(Level world, BlockPos from, double dirX, double dirZ) {
        BlockPos best = null;
        double bestDot = 0.0;
        BlockPos[] horizontals = { from.north(), from.south(), from.east(), from.west() };
        for (BlockPos h : horizontals) {
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos n = h.above(dy);
                if (!(world.getBlockState(n).getBlock() instanceof BaseRailBlock)) {
                    continue;
                }
                double dx = n.getX() - from.getX();
                double dz = n.getZ() - from.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                double dot = len < 1e-6 ? 0.0 : (dx * dirX + dz * dirZ) / len;
                if (dot > bestDot) {
                    bestDot = dot;
                    best = n;
                }
            }
        }
        return best;
    }
}
