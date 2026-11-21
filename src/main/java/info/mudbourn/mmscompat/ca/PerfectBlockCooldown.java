package info.mudbourn.mmscompat.ca;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

// Combat Amenities puts a shield on cooldown whenever it is lowered, and its parry
// force-lowers the shield (40 ticks against shield-disabling attackers). A block that
// lands inside a perfect window, either Combat Amenities' parry ticks or Mob AI Tweaks'
// Perfect Parry window, has that cooldown lifted at the end of the tick.
public final class PerfectBlockCooldown {

    private static final Identifier PERFECT_PARRY = Identifier.fromNamespaceAndPath("mob-ai-tweaks", "perfect_parry");
    private static final int MAIT_TICKS_PER_LEVEL = 10;
    private static final int CA_PARRY_FALLBACK = 5;
    // A weapon that blocks gets a weaker version: a shorter perfect window, and a perfect
    // block only cuts its cooldown to this many ticks instead of lifting it.
    private static final int WEAPON_WINDOW_TICKS = 2;
    private static final float WEAPON_WINDOW_SHARE = 0.5F;
    private static final int WEAPON_COOLDOWN_TICKS = 10;

    private static final Map<ServerPlayer, Pending> PENDING = new HashMap<>();
    private static int caParryTime = -1;

    private PerfectBlockCooldown() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PENDING.forEach((player, pending) -> {
                player.getCooldowns().removeCooldown(pending.group());
                if (pending.weapon()) {
                    player.getCooldowns().addCooldown(pending.group(), WEAPON_COOLDOWN_TICKS);
                }
            });
            PENDING.clear();
        });
    }

    // Called at the head of hurtServer, before Combat Amenities handles the block.
    public static void onHurt(ServerPlayer player, DamageSource source) {
        if (!player.isUsingItem()) {
            return;
        }
        ItemStack shield = player.getUseItem();
        if (!shield.has(DataComponents.BLOCKS_ATTACKS) || source.is(DamageTypeTags.BYPASSES_SHIELD)) {
            return;
        }
        Vec3 from = source.getSourcePosition();
        if (from == null || player.getViewVector(1.0F).dot(from.subtract(player.position()).normalize()) <= 0.0) {
            return;
        }
        if (inPerfectWindow(player, shield)) {
            PENDING.put(player, new Pending(player.getCooldowns().getCooldownGroup(shield), isBlockingWeapon(shield)));
        }
    }

    // Whether a raised shield is inside either perfect window. Shared with the client glow.
    public static boolean inPerfectWindow(LivingEntity entity, ItemStack shield) {
        int ticks = entity.getTicksUsingItem();
        boolean weapon = isBlockingWeapon(shield);
        if (ticks > 0 && ticks <= (weapon ? Math.min(WEAPON_WINDOW_TICKS, caParryTime()) : caParryTime())) {
            return true;
        }
        int level = 0;
        for (Holder<Enchantment> enchantment : shield.getEnchantments().keySet()) {
            if (enchantment.is(PERFECT_PARRY)) {
                level = shield.getEnchantments().getLevel(enchantment);
            }
        }
        int window = (level + 1) * MAIT_TICKS_PER_LEVEL;
        return level > 0 && ticks < (weapon ? Math.round(window * WEAPON_WINDOW_SHARE) : window);
    }

    // A weapon that can block, rather than a shield: it blocks and has its own attack damage.
    public static boolean isBlockingWeapon(ItemStack stack) {
        if (stack.getItem() instanceof ShieldItem) {
            return false;
        }
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return modifiers.modifiers().stream().anyMatch(entry -> entry.attribute().equals(Attributes.ATTACK_DAMAGE));
    }

    private record Pending(Identifier group, boolean weapon) {
    }

    // Combat Amenities' parry length, read once from its config class without linking to it.
    private static int caParryTime() {
        if (caParryTime < 0) {
            try {
                caParryTime = Class.forName("net.hollowed.combatamenities.config.CAConfig")
                    .getField("shieldParryTime").getInt(null);
            } catch (ClassNotFoundException e) {
                caParryTime = 0;
            } catch (ReflectiveOperationException | RuntimeException e) {
                caParryTime = CA_PARRY_FALLBACK;
            }
        }
        return caParryTime;
    }
}
