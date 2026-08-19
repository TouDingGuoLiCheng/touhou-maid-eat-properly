package com.touhoumaid.eatproperly.food;

import java.util.Collection;

/**
 * Combines multiple equipped Order Books: a food is allowed only when every book allows it
 * (intersection of each book's filtered pool).
 */
public final class OrderBookIntersection {
    private OrderBookIntersection() {
    }

    /**
     * {@code null} or empty {@code books} means no Order Book equipped → everything allowed
     * (caller still applies the global edible/safety pool).
     */
    public static boolean allowsAll(Collection<OrderBookSnapshot> books, String candidateId) {
        if (books == null || books.isEmpty()) {
            return true;
        }
        for (OrderBookSnapshot book : books) {
            if (book == null || !book.allows(candidateId)) {
                return false;
            }
        }
        return true;
    }
}
