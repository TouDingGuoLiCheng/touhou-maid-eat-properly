package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SatietySaturationOverlayTest {

    @Test
    void fullSaturationUsesFullOverlay() {
        assertEquals(SatietySaturationOverlay.Kind.FULL, SatietySaturationOverlay.kindForIcon(2.1F, 0));
        assertEquals(12, SatietySaturationOverlay.destWidth(SatietySaturationOverlay.Kind.FULL, 12));
        assertEquals(16, SatietySaturationOverlay.srcWidth(SatietySaturationOverlay.Kind.FULL, 16));
    }

    @Test
    void halfSaturationUsesLeftHalfOnlySoBaseCakeRightRemains() {
        assertEquals(SatietySaturationOverlay.Kind.HALF_LEFT, SatietySaturationOverlay.kindForIcon(1.0F, 0));
        assertEquals(6, SatietySaturationOverlay.destWidth(SatietySaturationOverlay.Kind.HALF_LEFT, 12));
        assertEquals(8, SatietySaturationOverlay.srcWidth(SatietySaturationOverlay.Kind.HALF_LEFT, 16));
        assertTrue(SatietySaturationOverlay.usesFullCakeUv(SatietySaturationOverlay.Kind.HALF_LEFT));
    }

    @Test
    void noSaturationDrawsNothing() {
        assertEquals(SatietySaturationOverlay.Kind.NONE, SatietySaturationOverlay.kindForIcon(0.0F, 0));
        assertEquals(0, SatietySaturationOverlay.destWidth(SatietySaturationOverlay.Kind.NONE, 12));
    }

    @Test
    void halfSaturationNeedsNormalRightUnderlayThenGoldLeft() {
        assertTrue(SatietySaturationOverlay.needsNormalCakeRightUnderlay(
                SatietySaturationOverlay.Kind.HALF_LEFT));
        assertFalse(SatietySaturationOverlay.needsNormalCakeRightUnderlay(
                SatietySaturationOverlay.Kind.FULL));
        assertEquals(6, SatietySaturationOverlay.halfDestOffset(12));
        assertEquals(8, SatietySaturationOverlay.halfSrcUOffset(16));
    }

    @Test
    void laterIconsUseSameTwoPointBands() {
        assertEquals(SatietySaturationOverlay.Kind.HALF_LEFT, SatietySaturationOverlay.kindForIcon(3.0F, 1));
        assertEquals(SatietySaturationOverlay.Kind.FULL, SatietySaturationOverlay.kindForIcon(4.0F, 1));
        assertEquals(SatietySaturationOverlay.Kind.NONE, SatietySaturationOverlay.kindForIcon(2.0F, 1));
    }
}
