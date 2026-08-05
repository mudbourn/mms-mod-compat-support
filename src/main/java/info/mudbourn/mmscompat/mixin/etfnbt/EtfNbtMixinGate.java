package info.mudbourn.mmscompat.mixin.etfnbt;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;
import java.util.Set;

/**
 * Gates the NBT fast path on Entity Texture Features being present.
 *
 * <p>{@code NBTProperty} is ETF's class, so without ETF there is nothing to target
 * and the mixin would fail to apply rather than quietly doing nothing. EMF is not
 * required: the {@code nbt(...)} animation function routes through ETF's property
 * regardless, and a pack using ETF's own {@code random entity} properties gets the
 * same saving.
 */
public class EtfNbtMixinGate implements IMixinConfigPlugin {

    private final boolean present =
            FabricLoader.getInstance().isModLoaded("entity_texture_features");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }

    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return present;
    }

    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
}
