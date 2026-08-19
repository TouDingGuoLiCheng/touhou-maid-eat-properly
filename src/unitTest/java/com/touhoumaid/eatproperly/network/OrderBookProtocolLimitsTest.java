package com.touhoumaid.eatproperly.network;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookProtocolLimitsTest {
    @Test
    void maxListedIdsCoversLargeModPackPools() {
        assertTrue(OrderBookProtocolLimits.MAX_LISTED_IDS >= 2048,
                "full-select pools can exceed 700 ids (observed 718)");
    }

    @Test
    void countWithinLimitIsAccepted() {
        assertTrue(OrderBookProtocolLimits.isValidListedIdCount(718));
        assertTrue(OrderBookProtocolLimits.isValidListedIdCount(0));
        assertTrue(OrderBookProtocolLimits.isValidListedIdCount(OrderBookProtocolLimits.MAX_LISTED_IDS));
    }

    @Test
    void countOutOfRangeRejectedWithoutNeedingThrow() {
        assertFalse(OrderBookProtocolLimits.isValidListedIdCount(-1));
        assertFalse(OrderBookProtocolLimits.isValidListedIdCount(OrderBookProtocolLimits.MAX_LISTED_IDS + 1));
    }

    @Test
    void boundedListedIdsTruncatesPolitely() {
        Set<String> ids = new LinkedHashSet<>();
        for (int i = 0; i < OrderBookProtocolLimits.MAX_LISTED_IDS + 10; i++) {
            ids.add("mod:food_" + i);
        }
        assertEquals(OrderBookProtocolLimits.MAX_LISTED_IDS,
                OrderBookProtocolLimits.boundedListedIds(ids).size());
    }
}
