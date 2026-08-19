package com.touhoumaid.eatproperly.item;

/**
 * Pure rules for lunchbox → maid-backpack delivery.
 * Full stack must fit; otherwise refuse (no partial hand-in, no request complete).
 */
public final class LunchboxDeliveryPolicy {
    private LunchboxDeliveryPolicy() {
    }

    /** {@code true} only when every item would be accepted (simulate leftover empty). */
    public static boolean canFullyInsert(int foodCount, int simulatedLeftoverCount) {
        return foodCount > 0 && simulatedLeftoverCount == 0;
    }

    /** After a real insert: success only if nothing remains. */
    public static boolean wasFullyInserted(int foodCount, int realLeftoverCount) {
        return canFullyInsert(foodCount, realLeftoverCount);
    }
}
