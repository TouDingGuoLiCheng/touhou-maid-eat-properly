package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookFilterRuleTest {
    private static final Set<String> LIST = Set.of("minecraft:apple", "minecraft:bread");

    @Test
    void emptyBlacklistAllowsEverything() {
        assertTrue(OrderBookFilterRule.allows(OrderBookFilterMode.BLACKLIST, Set.of(), "minecraft:apple"));
        assertTrue(OrderBookFilterRule.allows(OrderBookFilterMode.BLACKLIST, Set.of(), "minecraft:cooked_beef"));
    }

    @Test
    void emptyWhitelistAllowsNothing() {
        assertFalse(OrderBookFilterRule.allows(OrderBookFilterMode.WHITELIST, Set.of(), "minecraft:apple"));
    }

    @Test
    void whitelistOnlyAllowsListedIds() {
        assertTrue(OrderBookFilterRule.allows(OrderBookFilterMode.WHITELIST, LIST, "minecraft:apple"));
        assertFalse(OrderBookFilterRule.allows(OrderBookFilterMode.WHITELIST, LIST, "minecraft:cooked_beef"));
    }

    @Test
    void blacklistRejectsListedIds() {
        assertFalse(OrderBookFilterRule.allows(OrderBookFilterMode.BLACKLIST, LIST, "minecraft:bread"));
        assertTrue(OrderBookFilterRule.allows(OrderBookFilterMode.BLACKLIST, LIST, "minecraft:cooked_beef"));
    }
}
