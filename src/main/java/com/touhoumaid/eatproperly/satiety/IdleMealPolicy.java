package com.touhoumaid.eatproperly.satiety;

/**
 * Pure rules for idle-period work-meal attempts (A6.3 B: work + idle may eat; sleep may not).
 */
public final class IdleMealPolicy {
    private IdleMealPolicy() {
    }

    /**
     * Whether this mod should drive a work-meal-like eat outside TLM's WORK brain activity.
     *
     * @param activityName {@link net.minecraft.world.entity.schedule.Activity#getName()} or null
     */
    public static boolean shouldAttemptIdleEat(String activityName, boolean sleeping, boolean canBrainMoving) {
        if (sleeping || !canBrainMoving || activityName == null || activityName.isEmpty()) {
            return false;
        }
        if ("rest".equals(activityName) || "work".equals(activityName)) {
            return false;
        }
        return true;
    }

    /** Satiety gate shared with work meals. */
    public static boolean satietyAllows(int satiety, int eatWhenSatietyBelow) {
        return MaidMealHungerPolicy.canAttemptWorkOrHomeMeal(satiety, eatWhenSatietyBelow);
    }
}
