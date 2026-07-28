package com.touhoumaid.eatproperly.item.reward;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DualLunchboxLogicTest {
    @Test
    void completesWhenOneSlotMatchesAndOtherIsInPool() {
        assertTrue(DualLunchboxLogic.canComplete(true, true, false, true));
        assertTrue(DualLunchboxLogic.canComplete(false, true, true, true));
    }

    @Test
    void completesWhenBothSlotsMatch() {
        assertTrue(DualLunchboxLogic.canComplete(true, true, true, true));
    }

    @Test
    void rejectsWhenNeitherSlotMatchesRequest() {
        assertFalse(DualLunchboxLogic.canComplete(false, true, false, true));
    }

    @Test
    void rejectsWhenAnySlotIsNotEvenInFoodPool() {
        // Slot A "matches" the request but slot B isn't valid pool food at all.
        assertFalse(DualLunchboxLogic.canComplete(true, true, false, false));
        assertFalse(DualLunchboxLogic.canComplete(false, false, true, true));
    }
}
