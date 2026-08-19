package com.touhoumaid.eatproperly.food;

import java.util.Locale;

/** Pure text match for Order Book UI search (id + display name). */
public final class OrderBookListFilter {
    private OrderBookListFilter() {
    }

    /** Empty/blank query matches everything. */
    public static boolean matches(String query, String itemId, String displayName) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(itemId, q) || containsIgnoreCase(displayName, q);
    }

    private static boolean containsIgnoreCase(String haystack, String needleLower) {
        if (haystack == null || haystack.isEmpty()) {
            return false;
        }
        return haystack.toLowerCase(Locale.ROOT).contains(needleLower);
    }
}
