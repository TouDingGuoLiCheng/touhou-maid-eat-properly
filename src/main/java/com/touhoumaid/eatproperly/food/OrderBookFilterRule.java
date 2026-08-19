package com.touhoumaid.eatproperly.food;

import java.util.Set;

/** Pure list-matching rules for the Order Book (no Minecraft types). */
public final class OrderBookFilterRule {
    private OrderBookFilterRule() {
    }

    public static boolean allows(OrderBookFilterMode mode, Set<String> listedIds, String candidateId) {
        boolean listed = listedIds.contains(candidateId);
        return mode == OrderBookFilterMode.WHITELIST ? listed : !listed;
    }
}
