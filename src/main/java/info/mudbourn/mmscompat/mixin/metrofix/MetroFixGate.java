package info.mudbourn.mmscompat.mixin.metrofix;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;
import java.util.Set;

/**
 * Gates the ModMetro seating + localization patches on ModMetro alone.
 *
 * Deliberately NOT the same gate as {@code MetroMixinGate}: that one also
 * requires ACE, because a speed mismatch only exists when ACE is present.
 * These patches are independent of ACE and must apply whenever ModMetro is.
 */
public class MetroFixGate implements IMixinConfigPlugin {

    private final boolean present = FabricLoader.getInstance().isModLoaded("modmetro");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return present; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
}
