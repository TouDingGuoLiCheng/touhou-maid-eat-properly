package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;

/**
 * A6.3 B: while the brain is not in WORK (and not sleeping/rest), drive one work-meal-like eat
 * so idle maids still snack. WORK activity keeps using TLM {@code MaidWorkMealTask}.
 */
public final class IdleWorkMealHandler {
    private IdleWorkMealHandler() {
    }

    public static void tick(EntityMaid maid) {
        if (maid.level().isClientSide()) {
            return;
        }
        if (!IdleMealPolicy.shouldAttemptIdleEat(
                maid.getScheduleDetail() == null ? null : maid.getScheduleDetail().getName(),
                maid.isSleeping(),
                maid.canBrainMoving())) {
            return;
        }
        SatietyData.ensureInitialized(maid);
        int satiety = SatietyData.getSatiety(maid);
        int gate = EatProperlyConfig.EAT_WHEN_SATIETY_BELOW.get();
        if (!IdleMealPolicy.satietyAllows(satiety, gate)) {
            return;
        }
        // Respect WorkMeal favor cooldown except during hungry binge with edible food.
        int bingeUntil = EatProperlyConfig.HUNGRY_BINGE_UNTIL_SATIETY.get();
        boolean hasFood = HungerBackpackMeal.hasEdibleFood(maid);
        if (!hasFood) {
            return;
        }
        boolean binge = MaidMealHungerPolicy.shouldBypassMealCooldown(satiety, bingeUntil, true);
        if (!binge && !maid.getFavorabilityManager().canAdd(Type.WORK_MEAL.getTypeName())) {
            return;
        }
        HungerBackpackMeal.tryEatOne(maid);
    }
}
