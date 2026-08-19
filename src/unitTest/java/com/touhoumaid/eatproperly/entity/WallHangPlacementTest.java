package com.touhoumaid.eatproperly.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WallHangPlacementTest {
    @Test
    void certificateHangPosIsAirBesideClickedFace() {
        assertArrayEquals(
                new int[]{5, 10, 3},
                WallHangPlacement.hangPos(5, 10, 2, 0, 0, 1, false)
        );
    }

    @Test
    void bannerHangPosIsOneBelowAirBesideClickedUpperBlock() {
        // Click upper plank at y=11 facing +Z → hang pos at air beside lower plank
        assertArrayEquals(
                new int[]{5, 10, 3},
                WallHangPlacement.hangPos(5, 11, 2, 0, 0, 1, true)
        );
    }

    @Test
    void bannerRequiresUpperAndLowerSupport() {
        assertTrue(WallHangPlacement.canHangTwoHigh(true, true));
        assertFalse(WallHangPlacement.canHangTwoHigh(true, false));
        assertFalse(WallHangPlacement.canHangTwoHigh(false, true));
    }

    @Test
    void oneByOneUsesNonMultipleOf32HeightSoEntityCentersInHangBlock() {
        assertEquals(12, WallHangPlacement.hangPixelHeight(false));
        assertEquals(32, WallHangPlacement.hangPixelHeight(true));
        assertEquals(16, WallHangPlacement.hangPixelWidth());
    }
}
