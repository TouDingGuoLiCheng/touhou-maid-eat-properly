package com.touhoumaid.eatproperly.snackraid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnackRaidSearchBoundsTest {
    @Test
    void containsRespectsHorizontalRadiusAndVerticalRange() {
        assertTrue(SnackRaidSearchBounds.contains(0, 64, 0, 8, 67, 0, 8, 3));
        assertTrue(SnackRaidSearchBounds.contains(0, 64, 0, -8, 61, 8, 8, 3));
        assertFalse(SnackRaidSearchBounds.contains(0, 64, 0, 9, 64, 0, 8, 3));
        assertFalse(SnackRaidSearchBounds.contains(0, 64, 0, 0, 68, 0, 8, 3));
    }
}
