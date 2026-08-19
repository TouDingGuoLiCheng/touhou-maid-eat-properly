package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadedTimePolicyTest {
    @Test
    void firstSightingIsNotAGap() {
        assertFalse(LoadedTimePolicy.isUnloadGap(-1L, 1000L, 80L));
    }

    @Test
    void smallDeltaIsNotAGap() {
        assertFalse(LoadedTimePolicy.isUnloadGap(1000L, 1020L, 80L));
    }

    @Test
    void largeDeltaIsUnloadGap() {
        assertTrue(LoadedTimePolicy.isUnloadGap(1000L, 5000L, 80L));
    }

    @Test
    void advanceIgnoresDeltaOnGap() {
        assertEquals(100L, LoadedTimePolicy.advanceElapsed(100L, 4000L, true));
        assertEquals(120L, LoadedTimePolicy.advanceElapsed(100L, 20L, false));
    }

    @Test
    void staggeredOffsetsSpreadAcrossWindow() {
        assertEquals(3, LoadedTimePolicy.staggeredOffsets(3, 16000, 100, 600).length);
        int[] offsets = LoadedTimePolicy.staggeredOffsets(3, 16000, 100, 600);
        assertEquals(100, offsets[0]);
        assertEquals(700, offsets[1]);
        assertEquals(1300, offsets[2]);
    }

    @Test
    void staggeredOffsetsWrapInWindow() {
        int[] offsets = LoadedTimePolicy.staggeredOffsets(2, 1000, 900, 200);
        assertEquals(900, offsets[0]);
        assertEquals(100, offsets[1]);
    }
}
