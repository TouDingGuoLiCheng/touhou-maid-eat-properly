package com.touhoumaid.eatproperly.food;

/**
 * Pure rule: work/home/hunger meals may only consume effective-pool food.
 */
public final class EffectivePoolEatPolicy {
    private EffectivePoolEatPolicy() {
    }

    /** {@code true} when this edible is allowed for maid meal consumption. */
    public static boolean allows(boolean inEffectivePool) {
        return inEffectivePool;
    }
}
