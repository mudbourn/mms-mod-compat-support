package info.mudbourn.mmscompat.mixin.jobsplus;

import com.daqem.arc.api.action.IActionType;
import com.daqem.arc.data.ActionData;
import info.mudbourn.mmscompat.JobsPlusActionCooldown;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the ARC action type before {@code sendToAction()} runs and stores
 * the cooldown category in a ThreadLocal so the companion
 * {@link JobXpCooldownMixin} can read it.
 *
 * <p>This avoids the need for the XP mixin to depend on ARC classes — it only
 * reads the pre-computed {@link JobsPlusActionCooldown.CooldownCategory}.</p>
 */
@Mixin(ActionData.class)
public class ActionDataCooldownMixin {

    @Shadow private IActionType<?> actionType;

    @Inject(method = "sendToAction", at = @At("HEAD"))
    private void mmsCompat$captureCooldownType(CallbackInfo ci) {
        if (actionType != null) {
            String id = actionType.getIdentifier().toString();
            JobsPlusActionCooldown.setCooldownType(id);
        }
    }
}
