package com.touhoumaid.eatproperly.satiety;

/**
 * Picnic / home-meal hand gate: vanilla aborts when the held item cannot simulate-stow
 * into the backpack. E1 always allows the meal to proceed (hide-inv / drop handles restore).
 */
public final class HomeMealHandsPolicy {
    private HomeMealHandsPolicy() {
    }

    /** Vanilla would abort when the simulate-stow leftover is non-empty; we never abort. */
    public static boolean shouldAbortForHandFull(boolean simulateStowLeftoverNonEmpty) {
        return false;
    }

    /**
     * When simulate-stow fails, clear the leftover so {@code MaidHomeMealTask} continues.
     * Non-simulate calls are left unchanged.
     */
    public static boolean shouldClearSimulateLeftover(boolean simulate) {
        return simulate;
    }
}
