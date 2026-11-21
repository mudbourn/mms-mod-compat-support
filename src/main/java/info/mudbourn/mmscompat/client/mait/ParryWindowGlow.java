package info.mudbourn.mmscompat.client.mait;

import info.mudbourn.mmscompat.ca.PerfectBlockCooldown;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

// Lights up the local player's shield while Mob AI Tweaks' perfect parry window is open.
public final class ParryWindowGlow {


    private ParryWindowGlow() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ParryWindowGlow::sparkle);
    }

    // Whether this stack is the local player's raised shield inside its perfect parry window.
    public static boolean glowing(ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && inWindow(player) && ItemStack.isSameItemSameComponents(player.getUseItem(), stack);
    }

    private static boolean inWindow(LivingEntity entity) {
        if (!entity.isUsingItem()) {
            return false;
        }
        ItemStack using = entity.getUseItem();
        if (!using.has(DataComponents.BLOCKS_ATTACKS)) {
            return false;
        }
        // Mob AI Tweaks' Perfect Parry window, or Combat Amenities' parry ticks.
        return PerfectBlockCooldown.inPerfectWindow(entity, using);
    }

    private static void sparkle(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || !inWindow(player)) {
            return;
        }
        boolean right = (player.getUsedItemHand() == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT);
        Vec3 look = player.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0.0, look.x).normalize().scale(right ? 0.35 : -0.35);
        Vec3 at = player.getEyePosition().add(look.scale(0.6)).add(side).add(0.0, -0.25, 0.0);
        client.level.addParticle(
            ParticleTypes.ELECTRIC_SPARK,
            at.x + (player.getRandom().nextDouble() - 0.5) * 0.3,
            at.y + (player.getRandom().nextDouble() - 0.5) * 0.3,
            at.z + (player.getRandom().nextDouble() - 0.5) * 0.3,
            0.0,
            0.0,
            0.0
        );
    }
}
