package com.touhoumaid.eatproperly.satiety;

/**
 * Pure rules for TLM heal-self meals.
 * <p>
 * TLM's {@code DefaultMaidHealSelfMeal} grants no favor, so it never writes a favor cooldown, and
 * {@code MaidHealSelfTask} only checks "missing at least 2 HP". Without these gates an injured maid
 * re-opens the meal every ~50 ticks at any satiety, which reads in-game as endless binge eating
 * that raises satiety but never favor.
 */
public final class HealMealPolicy {
    /** Matches the TLM WorkMeal favor cooldown so heal eating paces like a normal meal. */
    public static final int DEFAULT_COOLDOWN_TICKS = 3600;

    private HealMealPolicy() {
    }

    /** Heal-self meals now share the work/home meal satiety gate. */
    public static boolean satietyAllows(int satiety, int eatWhenSatietyBelow) {
        return MaidMealHungerPolicy.canAttemptWorkOrHomeMeal(satiety, eatWhenSatietyBelow);
    }

    /**
     * @param lastEatTime  game time of the last heal-self eat, negative when never eaten
     * @param cooldownTicks {@code <= 0} disables pacing
     */
    public static boolean cooldownReady(long lastEatTime, long now, int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return true;
        }
        // Negative stamp = never ate; a stamp in the future means the world clock moved back.
        if (lastEatTime < 0L || now < lastEatTime) {
            return true;
        }
        return now - lastEatTime >= cooldownTicks;
    }

    public static long remainingCooldownTicks(long lastEatTime, long now, int cooldownTicks) {
        if (cooldownReady(lastEatTime, now, cooldownTicks)) {
            return 0L;
        }
        return cooldownTicks - (now - lastEatTime);
    }

    public static boolean allowsEat(int satiety, int eatWhenSatietyBelow,
                                    long lastEatTime, long now, int cooldownTicks) {
        return satietyAllows(satiety, eatWhenSatietyBelow)
                && cooldownReady(lastEatTime, now, cooldownTicks);
    }
}
