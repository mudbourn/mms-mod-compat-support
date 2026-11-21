package info.mudbourn.mmscompat.mixin.combatamenities;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;
import java.util.Set;

/**
 * Gates the Combat Amenities patch on Combat Amenities being loaded.
 */
public class CombatAmenitiesMixinGate implements IMixinConfigPlugin {

    private final boolean present = FabricLoader.getInstance().isModLoaded("combatamenities");
    private final boolean trinkets = FabricLoader.getInstance().isModLoaded("trinkets") || FabricLoader.getInstance().isModLoaded("trinkets_updated");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return present && (trinkets || !mixinClassName.endsWith("TrinketsAnchorOrderMixin"));
    }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
}
