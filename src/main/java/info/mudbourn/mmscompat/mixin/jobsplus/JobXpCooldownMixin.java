package info.mudbourn.mmscompat.mixin.jobsplus;

import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.player.JobsPlayer;
import info.mudbourn.mmscompat.JobsPlusActionCooldown;
import info.mudbourn.mmscompat.JobsPlusActionCooldown.CooldownCategory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Blocks XP grants that arrive during the hard-cooldown window.
 *
 * The companion {@link ActionDataCooldownMixin} sets the ThreadLocal category
 * before {@code sendToAction()} runs; this mixin reads it at the point
 * {@code addExperience} is called.  If the category is on cooldown the XP is
 * zeroed; otherwise the cooldown is armed and XP passes through at 100%.
 *
 * <p>{@code addExperienceWithoutEvent} (admin/command path) is left alone.</p>
 */
@Mixin(targets = "com.daqem.jobsplus.player.job.Job")
public class JobXpCooldownMixin {

    @Shadow private JobsPlayer player;
    @Shadow @Final private JobInstance jobInstance;

    @ModifyVariable(method = "addExperience(D)V", at = @At("HEAD"), argsOnly = true)
    private double mmsCompat$cooldownBlockXp(double experience) {
        if (experience <= 0 || player == null || jobInstance == null) return experience;

        Player serverPlayer = player.jobsplus$getPlayer();
        if (serverPlayer == null || serverPlayer.level().isClientSide()) return experience;

        CooldownCategory category = JobsPlusActionCooldown.getCooldownType();
        // Clean up ThreadLocal after reading
        JobsPlusActionCooldown.clearCooldownType();

        // NONE means the action type was not recognised — default to HARD
        if (category == CooldownCategory.NONE) {
            category = CooldownCategory.HARD;
        }

        long gameTime = serverPlayer.level().getGameTime();
        String jobId = jobInstance.getIdentifier().toString();

        if (JobsPlusActionCooldown.isOnCooldown(serverPlayer.getUUID(), jobId, category, gameTime)) {
            return 0.0; // block XP entirely
        }

        // Not on cooldown — arm it and let XP through at full value
        JobsPlusActionCooldown.setCooldown(serverPlayer.getUUID(), jobId, category, gameTime);
        return experience;
    }
}
