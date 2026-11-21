package info.mudbourn.mmscompat.mixin.rwa;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Reads an arrow's base damage.
@Mixin(AbstractArrow.class)
public interface ArrowBaseDamageAccessor {

    @Accessor("baseDamage")
    double mmsCompat$baseDamage();
}
