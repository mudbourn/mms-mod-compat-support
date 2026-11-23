package info.mudbourn.mmscompat.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

public class MmsModCompatSupportClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // ETF NBT fast path. Loaded unconditionally so the file and /mmsnbt exist
        // even without ETF installed; the mixin itself is gated on the mod.
        info.mudbourn.mmscompat.client.etfnbt.NbtTuning.load();
        info.mudbourn.mmscompat.client.etfnbt.NbtCommand.register();
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("trashslot")) {
            info.mudbourn.mmscompat.client.trashslot.QuickTrash.register();
        }
        if (FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            SharedWaypointClient.register();
            XaeroGlobalWaypointBridge.register();
        }
        if (FabricLoader.getInstance().isModLoaded("alexscaves") && FabricLoader.getInstance().isModLoaded("trinkets")) {
            info.mudbourn.mmscompat.client.alexscaves.WornPossessionKey.register();
        }
        if (FabricLoader.getInstance().isModLoaded("mr_bubblellaneous")) {
            BubbleClient.register();
        }
        if (FabricLoader.getInstance().isModLoaded("chatpatches")) {
            ChatBoundaryReveal.register();
        }
        if (FabricLoader.getInstance().isModLoaded("combatamenities")) {
            info.mudbourn.mmscompat.ca.ParryStreak.setLocalPlayer(() -> net.minecraft.client.Minecraft.getInstance().player);
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                info.mudbourn.mmscompat.ca.ParryStreak.StreakS2C.TYPE,
                (payload, context) -> info.mudbourn.mmscompat.ca.ParryStreak.onClientStreak(payload.ticks()));
        }
        if (FabricLoader.getInstance().isModLoaded("mob-ai-tweaks")) {
            info.mudbourn.mmscompat.client.mait.ParryWindowGlow.register();
        }
        if (FabricLoader.getInstance().isModLoaded("spell_engine")) {
            info.mudbourn.mmscompat.client.spellengine.SpellHotbarToggle.register();
        }
        if (FabricLoader.getInstance().isModLoaded("leawind_third_person")) {
            CrampedFirstPerson.register();
        }
        if (FabricLoader.getInstance().isModLoaded("particlerain")) {
            ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> ParticleRainBiomeMemo.reset());
        }
    }
}
