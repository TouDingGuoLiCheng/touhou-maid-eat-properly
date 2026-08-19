package com.touhoumaid.eatproperly.effect;

/**
 * Pure gate for maid Happiness satiety restore on {@code LivingTick}.
 * Kept separate from {@link HappinessEffect} so player hunger restore stays on the vanilla
 * effect-tick path and maid restore does not depend on whether TLM ticks custom MobEffects.
 */
public final class HappinessMaidRestorePolicy {
    private HappinessMaidRestorePolicy() {
    }

    public static boolean shouldRestoreThisTick(boolean hasHappinessEffect, int remainingDuration) {
        return hasHappinessEffect && HappinessTiming.isHungerRestoreTick(remainingDuration);
    }
}
