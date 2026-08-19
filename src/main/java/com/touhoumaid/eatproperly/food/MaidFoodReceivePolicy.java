package com.touhoumaid.eatproperly.food;

/**
 * Pure rules for whether a maid can accept food into hands and/or visible backpack.
 */
public final class MaidFoodReceivePolicy {
    private MaidFoodReceivePolicy() {
    }

    /** {@code true} when both native hands already hold an item. */
    public static boolean bothHandsOccupied(boolean mainHandEmpty, boolean offHandEmpty) {
        return !mainHandEmpty && !offHandEmpty;
    }

    /**
     * Full stack fits when any hand is free, or the simulated backpack insert leaves nothing.
     */
    public static boolean canFullyReceive(boolean hasEmptyHand, int foodCount, int backpackSimulatedLeftover) {
        if (foodCount <= 0) {
            return false;
        }
        if (hasEmptyHand) {
            return true;
        }
        return backpackSimulatedLeftover == 0;
    }

    /** Reject delivery only when both hands are full and the backpack cannot fit the stack. */
    public static boolean shouldRejectDelivery(
            boolean mainHandEmpty, boolean offHandEmpty, int foodCount, int backpackSimulatedLeftover) {
        return bothHandsOccupied(mainHandEmpty, offHandEmpty)
                && !canFullyReceive(false, foodCount, backpackSimulatedLeftover);
    }
}
