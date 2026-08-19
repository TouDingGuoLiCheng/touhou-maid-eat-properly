package com.touhoumaid.eatproperly.effect;

/**
 * Pure timing math for the Happiness buff (no Minecraft types) so unit tests can cover it
 * without booting Forge. See {@link HappinessEffect} for the Minecraft-side wiring.
 */
public final class HappinessTiming {
    /** 10 minutes, per the "eat properly" milestone reward spec. */
    public static final int DURATION_TICKS = 12_000;

    /** Favor≥80 proximity happiness refresh duration (3 minutes). */
    public static final int PROXIMITY_DURATION_TICKS = 3_600;

    /** Refresh when remaining duration is at or below this (aligned with trinket NV/speed). */
    public static final int PROXIMITY_REFRESH_THRESHOLD_TICKS = 20;

    /** Restore one hunger point every 20 ticks while the effect is active. */
    public static final int HUNGER_RESTORE_INTERVAL_TICKS = 20;
    public static final int HUNGER_RESTORE_AMOUNT = 1;

    /** Soft heart particles while Happiness is active (~0.75s). */
    public static final int HEART_PARTICLE_INTERVAL_TICKS = 15;

    private HappinessTiming() {
    }

    /**
     * Mirrors the contract of {@code MobEffect#isDurationEffectTick}: {@code remainingDuration}
     * counts down from {@link #DURATION_TICKS} to {@code 0}, checked before it decrements.
     */
    public static boolean isHungerRestoreTick(int remainingDuration) {
        return remainingDuration > 0 && remainingDuration % HUNGER_RESTORE_INTERVAL_TICKS == 0;
    }

    /** Gate for ambient heart particles using the entity's tick counter. */
    public static boolean isHeartParticleTick(int entityTickCount) {
        return entityTickCount > 0 && entityTickCount % HEART_PARTICLE_INTERVAL_TICKS == 0;
    }
}
