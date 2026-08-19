package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnerProximityPolicyTest {
    @Test
    void withinRangeWhenDistanceSqAtOrBelowLimit() {
        assertTrue(OwnerProximityPolicy.isWithinRange(0.0D, 16.0D));
        assertTrue(OwnerProximityPolicy.isWithinRange(256.0D, 16.0D));
    }

    @Test
    void outsideRangeWhenDistanceSqAboveLimit() {
        assertFalse(OwnerProximityPolicy.isWithinRange(256.01D, 16.0D));
        assertFalse(OwnerProximityPolicy.isWithinRange(1000.0D, 16.0D));
    }

    @Test
    void nonPositiveRangeNeverMatches() {
        assertFalse(OwnerProximityPolicy.isWithinRange(0.0D, 0.0D));
        assertFalse(OwnerProximityPolicy.isWithinRange(1.0D, -1.0D));
    }
}
