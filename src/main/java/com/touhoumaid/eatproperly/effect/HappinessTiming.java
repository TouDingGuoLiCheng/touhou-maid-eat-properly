package com.touhoumaid.eatproperly.effect;

/**
 * Pure timing math for the Happiness buff (no Minecraft types) so unit tests can cover it
 * without booting Forge. See {@link HappinessEffect} for the Minecraft-side wiring.
 */
public final class HappinessTiming {
    /** 10 minutes, per the "eat properly" milestone reward spec. */
    public static final int DURATION_TICKS = 12_000;

    /** Restore one hunger point every 20 ticks while the effect is active. */
    public static final int HUNGER_RESTORE_INTERVAL_TICKS = 20;
    public static final int HUNGER_RESTORE_AMOUNT = 1;

    private HappinessTiming() {
    }

    /**
     * Mirrors the contract of {@code MobEffect#isDurationEffectTick}: {@code remainingDuration}
     * counts down from {@link #DURATION_TICKS} to {@code 0}, checked before it decrements.
     */
    public static boolean isHungerRestoreTick(int remainingDuration) {
        return remainingDuration > 0 && remainingDuration % HUNGER_RESTORE_INTERVAL_TICKS == 0;
    }
}
