package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookListFilterTest {
    @Test
    void blankQueryMatchesAll() {
        assertTrue(OrderBookListFilter.matches("", "minecraft:bread", "Bread"));
        assertTrue(OrderBookListFilter.matches("   ", "minecraft:bread", "Bread"));
        assertTrue(OrderBookListFilter.matches(null, "minecraft:bread", "Bread"));
    }

    @Test
    void matchesIdOrDisplayNameCaseInsensitive() {
        assertTrue(OrderBookListFilter.matches("bread", "minecraft:bread", "Bread"));
        assertTrue(OrderBookListFilter.matches("BREAD", "minecraft:apple", "Bread"));
        assertTrue(OrderBookListFilter.matches("minecraft:app", "minecraft:apple", "Apple"));
        assertFalse(OrderBookListFilter.matches("beef", "minecraft:bread", "Bread"));
    }
}
