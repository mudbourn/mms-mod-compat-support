package info.mudbourn.mmscompat.client.etfnbt;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Serves ETF's {@code nbt(...)} lookups from live entity state instead of a full
 * entity serialisation.
 *
 * <h2>What it replaces</h2>
 *
 * <p>{@code NBTProperty#getEntityNBT} calls {@code ETFEntityRenderState#nbt()},
 * which reaches {@code NbtPredicate.getEntityTagToCompare(entity)} — i.e.
 * {@code entity.saveWithoutId(...)}, encoding the whole entity through Mojang
 * codecs, Trinkets and every Cardinal Component included. ETF caches the result in
 * a single static slot cleared by {@code setCurrentEntity}, so the price is one
 * full serialisation per entity per render pass, no matter how many rules ask.
 *
 * <p>On a 1.21.11 client that measured at 5.88% of the render thread, and it scales
 * with the number of visible players. The paths being read off that tag are four
 * fields that are all one accessor call away.
 *
 * <h2>How it stays honest</h2>
 *
 * <p>{@link #build} answers only for paths in {@link #SUPPORTED} and returns null
 * for anything else, which drops the caller back onto the real serialisation. A
 * pack, mod or DetailedAnimations update that asks for something new therefore gets
 * the slow answer rather than a wrong one. Nothing here can make a rule that works
 * today stop working; it can only fail to make it faster.
 *
 * <p>The synthesised tag holds the queried path and nothing else. That is enough
 * because {@code NBTProperty} resolves each rule by walking its own path from the
 * root, and — critically — the values written here are the same <em>tag types</em>
 * the real encoder would produce at those paths, so whatever ETF's matcher does
 * with them is unchanged. This deliberately does not reason about how {@code raw:}
 * or {@code iregex:} compare their input.
 *
 * <h2>Paths that cannot match on 1.21.11</h2>
 *
 * <p>Two of the four paths DetailedAnimations asks for are dead on this version,
 * and are claimed here so they stop costing a serialisation to answer "no":
 *
 * <ul>
 *   <li>{@code SleepingX} — 1.21.11 stores the sleeping position as a single
 *       {@code sleeping_pos} BlockPos. {@code SleepingX} survives only inside
 *       {@code InlineBlockPosFormatFix}, so it is never present on a live tag and
 *       {@code exists:true} is always false.</li>
 *   <li>{@code RootVehicle.*} — written by {@code ServerPlayer}'s save path, not by
 *       {@code Entity#saveWithoutId}. It is never present on the client tag ETF
 *       builds, so the {@code .*_boat} rule is always false.</li>
 * </ul>
 *
 * <p>Omitting them reproduces exactly that: the path does not resolve, the rule is
 * false, same as today. If a future version starts writing them, this class starts
 * lying — hence {@link NbtTuning#verify}, which runs both paths and reports any
 * disagreement.
 */
public final class NbtFastPath {

    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");

    /** The compound {@code LivingEntity} stores its worn and held stacks under. */
    private static final String EQUIPMENT_KEY = "equipment";

    /** Prefix for the per-slot equipment paths, e.g. {@code equipment.offhand.id}. */
    private static final String EQUIPMENT = EQUIPMENT_KEY + ".";

    /**
     * Paths this class will answer for.
     *
     * <p>Everything else falls through. Per-slot equipment paths are recognised by
     * {@link #equipmentSlot} rather than enumerated, since the slot set is vanilla's.
     */
    private static final Set<String> SUPPORTED = Set.of(
            "SelectedItem.id",
            // Dead on 1.21.11; claimed so they answer false for free. See javadoc.
            "SleepingX",
            "RootVehicle.Entity.id"
    );

    /** Mismatches already reported, so a disagreeing path logs once and not per frame. */
    private static final Set<String> REPORTED = new HashSet<>();

    private NbtFastPath() {
    }

    /**
     * A minimal tag covering {@code paths}, or null if any path is unsupported.
     *
     * <p>All-or-nothing on purpose: ETF's cache is a single slot shared by every
     * rule on the entity, so one rule falling back pays the full serialisation and
     * the rest would then be reading a tag that already exists. Answering only when
     * the whole set is covered keeps the two paths from interleaving.
     *
     * @param paths the NBT paths the calling property tests, as written in the pack
     * @param entity the entity being rendered
     */
    @Nullable
    public static CompoundTag build(Set<String> paths, Entity entity) {
        for (String path : paths) {
            if (!SUPPORTED.contains(path) && equipmentSlot(path) == null) {
                return null;
            }
        }

        CompoundTag root = new CompoundTag();
        for (String path : paths) {
            switch (path) {
                case "SelectedItem.id" -> {
                    // Vanilla stores SelectedItem for players only, and only when the
                    // held stack is non-empty. Both conditions are reproduced here so
                    // an empty hand leaves the path unresolvable, as it is today.
                    if (entity instanceof Player player) {
                        putItemId(root, "SelectedItem", player.getInventory().getSelectedItem());
                    }
                }
                case "SleepingX", "RootVehicle.Entity.id" -> {
                    // Intentionally absent. See the class javadoc.
                }
                default -> {
                    EquipmentSlot slot = equipmentSlot(path);
                    if (slot != null && entity instanceof LivingEntity living) {
                        CompoundTag equipment = root.getCompound(EQUIPMENT_KEY)
                                .orElseGet(CompoundTag::new);
                        putItemId(equipment, slot.getSerializedName(), living.getItemBySlot(slot));
                        if (!equipment.isEmpty()) {
                            root.put(EQUIPMENT_KEY, equipment);
                        }
                    }
                }
            }
        }
        return root;
    }

    /**
     * The slot an {@code equipment.<slot>.id} path names, or null if {@code path} is
     * not one of those.
     */
    @Nullable
    private static EquipmentSlot equipmentSlot(String path) {
        if (!path.startsWith(EQUIPMENT) || !path.endsWith(".id")) {
            return null;
        }
        String name = path.substring(EQUIPMENT.length(), path.length() - ".id".length());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getSerializedName().equals(name)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Writes {@code {<key>: {id: "<item id>"}}}, matching the shape
     * {@code ItemStack.CODEC} produces at the {@code id} field.
     *
     * <p>Only {@code id} is written. Every supported path ends in {@code .id}, and a
     * path that wanted {@code count} or {@code components} would not be in
     * {@link #SUPPORTED} and so would never reach here.
     */
    private static void putItemId(CompoundTag parent, String key, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag item = new CompoundTag();
        item.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        parent.put(key, item);
    }

    /**
     * Compares the fast tag against the real one at each queried path and logs any
     * disagreement once.
     *
     * <p>Only the queried paths are compared — the fast tag is minimal by design, so
     * comparing whole tags would report a difference on every call. Every supported
     * path is a chain of compound keys with no list indices, so descending by key is
     * the same resolution {@code NBTProperty} would do.
     *
     * @return true when the two agree on every path
     */
    public static boolean verify(Set<String> paths, CompoundTag fast, CompoundTag real) {
        boolean ok = true;
        for (String path : paths) {
            Tag fastValue = resolve(fast, path);
            Tag realValue = resolve(real, path);
            if (!java.util.Objects.equals(fastValue, realValue)) {
                ok = false;
                if (REPORTED.add(path)) {
                    LOG.warn("[mms_compat] nbt fast path disagrees at '{}': fast={} real={}."
                                    + " The fast path is wrong for this path — report it and"
                                    + " run /mmsnbt fast off until it is fixed.",
                            path, fastValue, realValue);
                }
            }
        }
        return ok;
    }

    /** Descends {@code tag} by dot-separated compound keys. Null if the path is absent. */
    @Nullable
    private static Tag resolve(CompoundTag tag, String path) {
        Tag current = tag;
        for (String key : path.split("\\.")) {
            if (!(current instanceof CompoundTag compound)) {
                return null;
            }
            current = compound.get(key);
            if (current == null) {
                return null;
            }
        }
        return current;
    }
}
