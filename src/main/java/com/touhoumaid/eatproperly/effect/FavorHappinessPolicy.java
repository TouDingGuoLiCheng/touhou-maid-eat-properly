package com.touhoumaid.eatproperly.effect;

/**
 * Pure eligibility→refresh decision for high-favor proximity Happiness.
 * Leaving range never clears; callers simply stop refreshing.
 */
public final class FavorHappinessPolicy {
    private FavorHappinessPolicy() {
    }

    /**
     * @param eligible              favor≥80 and owner within speech range (caller-computed)
     * @param currentDurationTicks  remaining Happiness duration, or 0 if absent
     */
    public static boolean shouldRefresh(boolean eligible, int currentDurationTicks) {
        if (!eligible) {
            return false;
        }
        return currentDurationTicks <= HappinessTiming.PROXIMITY_REFRESH_THRESHOLD_TICKS;
    }
}
