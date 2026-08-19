package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.touhoumaid.eatproperly.mixin.FavorabilityManagerAccessor;

import java.util.Map;

/**
 * Read / clear TLM WorkMeal &amp; HomeMeal favor cooldowns for debug commands.
 */
public final class MealFavorCooldown {
    private MealFavorCooldown() {
    }

    public static int remainingTicks(FavorabilityManager manager, String typeName) {
        Map<String, FavorabilityManager.Time> counter = counter(manager);
        FavorabilityManager.Time time = counter.get(typeName);
        if (time == null) {
            return 0;
        }
        return Math.max(0, time.getTickCount());
    }

    /** True when the cooldown map has no active entry (ignores this mod's binge canAdd bypass). */
    public static boolean rawCanAdd(FavorabilityManager manager, String typeName) {
        return remainingTicks(manager, typeName) <= 0;
    }

    public static void clearMealCooldowns(FavorabilityManager manager) {
        Map<String, FavorabilityManager.Time> counter = counter(manager);
        counter.remove(MaidMealHungerPolicy.WORK_MEAL_TYPE);
        counter.remove(MaidMealHungerPolicy.HOME_MEAL_TYPE);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, FavorabilityManager.Time> counter(FavorabilityManager manager) {
        return ((FavorabilityManagerAccessor) (Object) manager).eatproperly$getCounter();
    }
}
