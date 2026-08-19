package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodPermitPrunePolicyTest {

    @Test
    void onlySelectedServerSidePermitPrunesWhileHeld() {
        assertTrue(FoodPermitPrunePolicy.shouldPruneWhileHeld(true, true));
        assertFalse(FoodPermitPrunePolicy.shouldPruneWhileHeld(true, false));
        assertFalse(FoodPermitPrunePolicy.shouldPruneWhileHeld(false, true));
    }

    @Test
    void holdPruneRunsOnIntervalNotEveryTick() {
        int interval = 20;
        assertTrue(FoodPermitPrunePolicy.shouldPruneThisTick(0, interval));
        assertFalse(FoodPermitPrunePolicy.shouldPruneThisTick(1, interval));
        assertTrue(FoodPermitPrunePolicy.shouldPruneThisTick(20, interval));
        assertTrue(FoodPermitPrunePolicy.shouldPruneThisTick(40, interval));
    }
}
