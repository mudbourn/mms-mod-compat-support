package info.mudbourn.mmscompat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Computes the effective minecart speed scale for ModMetro carts,
 * accounting for ACE's raised speed cap.
 *
 * Baseline is 0.4 b/t (vanilla). Scale = effectiveMaxSpeed / 0.4.
 * Clamped to floor of 1.0 so a low ACE setting never shrinks braking.
 *
 * ACE gamerules are Integer in blocks/second (default: player=20, other=0, empty=0).
 * 0 means "use vanilla" — we treat it as the 0.4 b/t baseline.
 * Conversion: b/s ÷ 20 = b/t.
 *
 * Uses pure reflection for ACE gamerule access — no compile dependency on ACE.
 */
public class MetroSpeedScale {

    private static final double VANILLA_BASELINE = 0.4; // b/t
    private static final double BS_TO_BT = 1.0 / 20.0;  // blocks/second → blocks/tick

    // Per-tick cache: keyed on world tick count
    private static long cachedTick = -1;
    private static double cachedScale = 1.0;
    private static double cachedEffectiveMaxSpeed = VANILLA_BASELINE;

    // Reflection state — resolved once
    private static boolean initDone = false;
    private static boolean reflectionFailed = false;
    private static Method getGameRulesMethod;
    private static Field rulesMapField;
    private static Method ruleGetIdMethod;
    private static Method ruleGetValueMethod;

    /**
     * Returns the effective max speed (blocks/tick) for a metro cart.
     */
    public static double effectiveMaxSpeed(Entity cart) {
        updateCache(cart);
        return cachedEffectiveMaxSpeed;
    }

    /**
     * Returns the scale factor: effectiveMaxSpeed / 0.4, floored at 1.0.
     */
    public static double scale(Entity cart) {
        updateCache(cart);
        return cachedScale;
    }

    @SuppressWarnings("unchecked")
    private static void updateCache(Entity cart) {
        Level level = cart.level();
        if (level == null) return;

        long tick = level.getGameTime();
        if (tick == cachedTick) return;
        cachedTick = tick;

        double effective = VANILLA_BASELINE;

        // Try ACE gamerules via reflection
        if (level instanceof ServerLevel serverLevel && !reflectionFailed) {
            try {
                if (!initDone) {
                    initReflection(serverLevel);
                    initDone = true;
                }
                if (!reflectionFailed) {
                    Object rules = getGameRulesMethod.invoke(serverLevel);
                    Map<?, ?> map = (Map<?, ?>) rulesMapField.get(rules);

                    // ACE registers: speed_player (default 20), speed_other (default 0), speed_empty (default 0)
                    // Values are Integer blocks/second. 0 = vanilla.
                    String ruleName;
                    if (!cart.getPassengers().isEmpty()) {
                        ruleName = "speed_player";
                    } else {
                        ruleName = "speed_other";
                    }

                    for (Object entry : map.entrySet()) {
                        Object key = ((Map.Entry<?, ?>) entry).getKey();
                        String id = (String) ruleGetIdMethod.invoke(key);
                        if (ruleName.equals(id)) {
                            Object ruleValue = ((Map.Entry<?, ?>) entry).getValue();
                            String valueStr = (String) ruleGetValueMethod.invoke(ruleValue);
                            int aceSpeedBs = Integer.parseInt(valueStr);
                            if (aceSpeedBs > 0) {
                                effective = aceSpeedBs * BS_TO_BT;
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                reflectionFailed = true;
            }
        }

        cachedEffectiveMaxSpeed = effective;
        cachedScale = Math.max(1.0, effective / VANILLA_BASELINE);
    }

    private static void initReflection(ServerLevel serverLevel) {
        try {
            // ServerLevel.getGameRules() -> GameRules
            getGameRulesMethod = ServerLevel.class.getDeclaredMethod("getGameRules");
            getGameRulesMethod.setAccessible(true);

            Object rules = getGameRulesMethod.invoke(serverLevel);
            Class<?> rulesClass = rules.getClass();

            // GameRules.rules -> Map<Key<?>, RuleValue<?>>
            rulesMapField = rulesClass.getDeclaredField("rules");
            rulesMapField.setAccessible(true);

            // Find Key.getId() and RuleValue.get() methods
            Map<?, ?> map = (Map<?, ?>) rulesMapField.get(rules);
            if (!map.isEmpty()) {
                Map.Entry<?, ?> first = map.entrySet().iterator().next();
                ruleGetIdMethod = first.getKey().getClass().getMethod("getId");
                ruleGetIdMethod.setAccessible(true);
                ruleGetValueMethod = first.getValue().getClass().getMethod("get");
                ruleGetValueMethod.setAccessible(true);
            } else {
                reflectionFailed = true;
            }
        } catch (Exception e) {
            reflectionFailed = true;
        }
    }
}
