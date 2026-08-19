package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PicnicLeftoverPolicyTest {
    @Test
    void radiusIsSix() {
        assertEquals(6, PicnicLeftoverPolicy.SEARCH_RADIUS);
    }

    @Test
    void onlyAttemptsWhenPicnicMatKnown() {
        assertTrue(PicnicLeftoverPolicy.shouldAttemptPlacement(true));
        assertFalse(PicnicLeftoverPolicy.shouldAttemptPlacement(false));
    }

    @Test
    void skipsEmptyLeftover() {
        assertFalse(PicnicLeftoverPolicy.shouldAttemptPlacement(true, true));
        assertTrue(PicnicLeftoverPolicy.shouldAttemptPlacement(true, false));
    }

    @Test
    void chebyshevWithinRadius() {
        assertTrue(PicnicLeftoverPolicy.isWithinRadius(0, 0, 0, 6));
        assertTrue(PicnicLeftoverPolicy.isWithinRadius(6, 6, 6, 6));
        assertFalse(PicnicLeftoverPolicy.isWithinRadius(7, 0, 0, 6));
        assertFalse(PicnicLeftoverPolicy.isWithinRadius(0, 7, 0, 6));
    }

    @Test
    void matLookupRadiusIsAtLeastSearchRadius() {
        assertTrue(PicnicLeftoverPolicy.MAT_LOOKUP_RADIUS >= PicnicLeftoverPolicy.SEARCH_RADIUS);
    }

    @Test
    void usesTileCenterWhenKnown() {
        assertTrue(PicnicLeftoverPolicy.shouldUseTileCenter(true));
        assertFalse(PicnicLeftoverPolicy.shouldUseTileCenter(false));
    }
}
