package com.touhoumaid.eatproperly.satiety;

/**
 * Pure rules for when work/home meals may really eat, when to fake-skip into TLM cooldown,
 * and when TLM meal favor cooldowns are bypassed so a hungry maid can binge-eat up to a stop line.
 */
public final class MaidMealHungerPolicy {
    /** Default stop line for hungry binge (satiety &gt;= this → normal cooldown). */
    public static final int DEFAULT_BINGE_UNTIL = 10;

    /**
     * Default {@code eatWhenSatietyBelow}: real eat only when satiety &lt; this.
     * 16 means satiety &gt; 15 skips real eating (fake cooldown).
     */
    public static final int DEFAULT_EAT_WHEN_SATIETY_BELOW = 16;

    /** TLM favor type names for work / home meals. */
    public static final String WORK_MEAL_TYPE = "WorkMeal";
    public static final String HOME_MEAL_TYPE = "HomeMeal";

    private MaidMealHungerPolicy() {
    }

    /** Work/home meal: only really eat when satiety is strictly below {@code eatWhenSatietyBelow}. */
    public static boolean canAttemptWorkOrHomeMeal(int satiety, int eatWhenSatietyBelow) {
        return satiety < eatWhenSatietyBelow;
    }

    /**
     * When cooldown fired but satiety is too high: skip real eat (no food, no sound) and
     * apply TLM meal cooldown with 0 favor so the task does not treat picnic food as "empty".
     */
    public static boolean shouldFakeSkipMeal(int satiety, int eatWhenSatietyBelow) {
        return !canAttemptWorkOrHomeMeal(satiety, eatWhenSatietyBelow);
    }

    /** Bypass TLM meal cooldown while satiety is strictly below the binge stop line (default 10). */
    public static boolean shouldBypassMealCooldown(int satiety) {
        return shouldBypassMealCooldown(satiety, DEFAULT_BINGE_UNTIL);
    }

    public static boolean shouldBypassMealCooldown(int satiety, int bingeUntilSatiety) {
        return satiety < bingeUntilSatiety;
    }

    /**
     * Hungry binge may ignore TLM meal cooldown only when a real eat can happen.
     * Without edible food, bypassing leaves {@code canAdd=true} forever while the meal task
     * retries every ~50 ticks and never applies cooldown (empty spin / looks like continuous eating).
     */
    public static boolean shouldBypassMealCooldown(int satiety, int bingeUntilSatiety, boolean hasEdibleFood) {
        return hasEdibleFood && shouldBypassMealCooldown(satiety, bingeUntilSatiety);
    }

    public static boolean isMealFavorType(String typeName) {
        return WORK_MEAL_TYPE.equals(typeName) || HOME_MEAL_TYPE.equals(typeName);
    }

    /** Debug / status: where satiety sits relative to binge stop and eat gate. */
    public enum MealZone {
        /** satiety &lt; bingeUntil → TLM meal cooldown bypassed. */
        BINGE,
        /** bingeUntil ≤ satiety &lt; eatWhenBelow → real eat with normal TLM cooldown. */
        NORMAL_CD,
        /** satiety ≥ eatWhenBelow → fake-skip (no food). */
        FAKE_SKIP
    }

    public static MealZone mealZone(int satiety, int bingeUntil, int eatWhenBelow) {
        if (shouldFakeSkipMeal(satiety, eatWhenBelow)) {
            return MealZone.FAKE_SKIP;
        }
        if (shouldBypassMealCooldown(satiety, bingeUntil)) {
            return MealZone.BINGE;
        }
        return MealZone.NORMAL_CD;
    }
}
