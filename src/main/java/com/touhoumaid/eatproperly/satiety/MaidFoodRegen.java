package com.touhoumaid.eatproperly.satiety;

/**
 * Pure regen decision mirroring {@link net.minecraft.world.food.FoodData#tick}.
 */
public final class MaidFoodRegen {
    public static final int FAST_REGEN_INTERVAL = 10;
    public static final int SLOW_REGEN_INTERVAL = 80;
    public static final int FAST_REGEN_MIN_FOOD = 10;
    /** With saturation: vanilla-like cap for gold-layer spending. */
    public static final float FAST_REGEN_EXHAUSTION_CAP = 6.0F;
    /**
     * No-saturation fast path: exhaustion per 1 HP so that 2 food ≈ 20 HP
     * ({@code 20 * 0.4 / }{@link MaidFoodMath#EXHAUSTION_DROP}{@code = 2}).
     */
    public static final float NO_SAT_FAST_EXHAUSTION_PER_HP = 0.4F;

    private MaidFoodRegen() {
    }

    public enum RegenKind {
        NONE,
        FAST,
        SLOW
    }

    public static TickResult tick(
            int foodLevel,
            float saturationLevel,
            boolean injured,
            int tickTimer,
            boolean naturalRegeneration) {
        if (!naturalRegeneration || !injured) {
            return TickResult.noRegen(0);
        }
        // Fast path while food >= 10 and saturation remains (prefer spending gold layer).
        if (foodLevel >= FAST_REGEN_MIN_FOOD && saturationLevel > 0.0F) {
            int nextTimer = tickTimer + 1;
            if (nextTimer < FAST_REGEN_INTERVAL) {
                return new TickResult(RegenKind.NONE, 0.0F, 0.0F, nextTimer);
            }
            float exhaustion = Math.min(saturationLevel, FAST_REGEN_EXHAUSTION_CAP);
            float heal = exhaustion / FAST_REGEN_EXHAUSTION_CAP;
            return new TickResult(RegenKind.FAST, heal, exhaustion, 0);
        }
        // H2-regen2: after saturation is gone, keep fast-healing while food >= 10 (include 10);
        // stop only when food drops below 10. Cost ≈ 2 food per 20 HP.
        if (foodLevel >= FAST_REGEN_MIN_FOOD) {
            int nextTimer = tickTimer + 1;
            if (nextTimer < FAST_REGEN_INTERVAL) {
                return new TickResult(RegenKind.NONE, 0.0F, 0.0F, nextTimer);
            }
            return new TickResult(RegenKind.FAST, 1.0F, NO_SAT_FAST_EXHAUSTION_PER_HP, 0);
        }
        if (foodLevel < FAST_REGEN_MIN_FOOD) {
            int nextTimer = tickTimer + 1;
            if (nextTimer < SLOW_REGEN_INTERVAL) {
                return new TickResult(RegenKind.NONE, 0.0F, 0.0F, nextTimer);
            }
            return new TickResult(RegenKind.SLOW, 1.0F, 6.0F, 0);
        }
        return TickResult.noRegen(0);
    }

    public record TickResult(RegenKind kind, float healAmount, float exhaustionAdded, int tickTimer) {
        public static TickResult noRegen(int tickTimer) {
            return new TickResult(RegenKind.NONE, 0.0F, 0.0F, tickTimer);
        }

        public boolean shouldHeal() {
            return kind != RegenKind.NONE && healAmount > 0.0F;
        }
    }
}
