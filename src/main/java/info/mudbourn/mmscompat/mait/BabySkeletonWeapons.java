package info.mudbourn.mmscompat.mait;

import info.mudbourn.mmscompat.mixin.mait.MobEquipmentInvoker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

// A spawn egg used on a skeleton makes a Mob AI Tweaks baby without the usual spawn setup, so it came out unarmed.
// The first time such a baby loads empty-handed it gets the weapon its kind normally spawns with.
public final class BabySkeletonWeapons {

    private static final String ARMED_TAG = "mms_compat_baby_armed";

    private BabySkeletonWeapons() {
    }

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded("mob-ai-tweaks")) {
            return;
        }
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof AbstractSkeleton skeleton
                && skeleton.isBaby()
                && skeleton.getMainHandItem().isEmpty()
                && skeleton.addTag(ARMED_TAG)) {
                ((MobEquipmentInvoker) skeleton).mms_compat$populateDefaultEquipmentSlots(
                    skeleton.getRandom(),
                    level.getCurrentDifficultyAt(skeleton.blockPosition())
                );
                skeleton.reassessWeaponGoal();
            }
        });
    }
}
