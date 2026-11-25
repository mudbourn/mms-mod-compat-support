package info.mudbourn.mmscompat.mixin.mait;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobEquipmentInvoker {

    @Invoker("populateDefaultEquipmentSlots")
    void mms_compat$populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty);
}
