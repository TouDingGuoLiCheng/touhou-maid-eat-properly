package com.touhoumaid.eatproperly.satiety;

/**
 * Favor granted when a maid actually eats a work/home meal.
 * Replaces TLM's 0–1 roll with {@code floor(nutrition × 1.5)}.
 */
public final class MealFavorPolicy {
    private MealFavorPolicy() {
    }

    /**
     * Work/home meal favor from food nutrition (same value that restores maid satiety).
     * Uses integer {@code nutrition * 3 / 2} (= floor of nutrition × 1.5 for non-negative ints).
     */
    public static int pointsForNutrition(int nutrition) {
        if (nutrition <= 0) {
            return 0;
        }
        return nutrition * 3 / 2;
    }

    /**
     * Positive work/home meal favor must always apply (A9): satiety is restored at
     * {@code onMaidEat} HEAD before {@code apply}, so the binge {@code canAdd} bypass
     * often sees post-meal satiety and would otherwise swallow the second eat's points.
     * Fake-skip uses {@code apply(type, 0)} and must not force-grant.
     */
    public static boolean shouldForceGrantMealFavor(String typeName, int points) {
        return points > 0 && MaidMealHungerPolicy.isMealFavorType(typeName);
    }
}
