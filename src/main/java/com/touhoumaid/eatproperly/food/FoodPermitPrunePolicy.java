package com.touhoumaid.eatproperly.food;

/**
 * When a player holds a Food Permit, prune ghost marks on an interval (not every tick).
 * Checks at most {@link FoodPermitMarks#MAX} block entities — not a world scan.
 */
public final class FoodPermitPrunePolicy {
    /** Same cadence as picnic leave-to-fetch mark scan. */
    public static final int HOLD_PRUNE_INTERVAL_TICKS = 20;

    private FoodPermitPrunePolicy() {
    }

    public static boolean shouldPruneWhileHeld(boolean isSelected, boolean serverSide) {
        return isSelected && serverSide;
    }

    public static boolean shouldPruneThisTick(long gameTime, int intervalTicks) {
        if (intervalTicks <= 1) {
            return true;
        }
        return gameTime % intervalTicks == 0;
    }
}
