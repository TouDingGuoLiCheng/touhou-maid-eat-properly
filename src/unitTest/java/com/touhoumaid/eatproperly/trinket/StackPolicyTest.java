package com.touhoumaid.eatproperly.trinket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackPolicyTest {
    @Test
    void additiveScalesLinearlyWithCount() {
        assertEquals(0, StackPolicy.ADDITIVE.mergeInt(0, 1));
        assertEquals(1, StackPolicy.ADDITIVE.mergeInt(1, 1));
        assertEquals(3, StackPolicy.ADDITIVE.mergeInt(3, 1));
        assertEquals(6, StackPolicy.ADDITIVE.mergeInt(3, 2));
    }

    @Test
    void uniqueIgnoresCountBeyondOne() {
        assertEquals(0, StackPolicy.UNIQUE.mergeInt(0, 40));
        assertEquals(40, StackPolicy.UNIQUE.mergeInt(1, 40));
        assertEquals(40, StackPolicy.UNIQUE.mergeInt(2, 40));
        assertEquals(40, StackPolicy.UNIQUE.mergeInt(3, 40));
    }

    @Test
    void maxIgnoresCountBeyondOneJustLikeUnique() {
        assertEquals(0.0, StackPolicy.MAX.mergeDouble(0, 0.4));
        assertEquals(0.4, StackPolicy.MAX.mergeDouble(1, 0.4));
        assertEquals(0.4, StackPolicy.MAX.mergeDouble(2, 0.4));
    }

    @Test
    void isActiveReflectsCount() {
        assertFalse(StackPolicy.UNIQUE.isActive(0));
        assertTrue(StackPolicy.UNIQUE.isActive(1));
        assertTrue(StackPolicy.ADDITIVE.isActive(5));
    }

    @Test
    void maxOfPicksLargestAcrossDistinctItems() {
        assertEquals(0.4, StackPolicy.maxOf(0.4));
        assertEquals(0.4, StackPolicy.maxOf(0.1, 0.4, 0.2));
        assertEquals(0.0, StackPolicy.maxOf());
    }
}
