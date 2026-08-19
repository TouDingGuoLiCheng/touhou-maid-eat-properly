package com.touhoumaid.eatproperly.food;

/**
 * Pure picnic / handler scan helpers for food-request matching (no Minecraft types).
 */
public final class FoodHandlerMatch {
    private FoodHandlerMatch() {
    }

    /** True when any non-blank slot id equals {@code wantedId}. */
    public static boolean containsWanted(String[] slotItemIds, String wantedId) {
        if (wantedId == null || wantedId.isEmpty() || slotItemIds == null) {
            return false;
        }
        for (String id : slotItemIds) {
            if (id != null && !id.isEmpty() && wantedId.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
