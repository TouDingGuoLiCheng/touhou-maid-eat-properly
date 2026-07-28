package com.touhoumaid.eatproperly.item.reward;

/**
 * Pure two-slot completion rule (no Minecraft types) for the dual lunchbox:
 * at least one slot must satisfy the current request, and every filled slot must at least be a
 * valid food-pool item.
 */
public final class DualLunchboxLogic {
    private DualLunchboxLogic() {
    }

    public static boolean canComplete(boolean slotAMatchesRequest, boolean slotAInPool,
                                       boolean slotBMatchesRequest, boolean slotBInPool) {
        if (!slotAInPool || !slotBInPool) {
            return false;
        }
        return slotAMatchesRequest || slotBMatchesRequest;
    }
}
