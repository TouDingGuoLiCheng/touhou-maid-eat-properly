package com.touhoumaid.eatproperly.food;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Immutable Order Book filter stored on the item NBT. */
public record OrderBookSnapshot(OrderBookFilterMode mode, Set<String> listedIds) {
    public static final OrderBookSnapshot EMPTY_BLACKLIST =
            new OrderBookSnapshot(OrderBookFilterMode.BLACKLIST, Set.of());

    public OrderBookSnapshot(OrderBookFilterMode mode, Set<String> listedIds) {
        this.mode = mode == null ? OrderBookFilterMode.BLACKLIST : mode;
        this.listedIds = Collections.unmodifiableSet(new LinkedHashSet<>(listedIds == null ? Set.of() : listedIds));
    }

    public boolean allows(String candidateId) {
        return OrderBookFilterRule.allows(mode, listedIds, candidateId);
    }
}
