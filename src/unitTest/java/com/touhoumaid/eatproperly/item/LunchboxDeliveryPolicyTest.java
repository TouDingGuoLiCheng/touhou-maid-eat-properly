package com.touhoumaid.eatproperly.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LunchboxDeliveryPolicyTest {
    @Test
    void refusesWhenBackpackCannotTakeAll() {
        assertFalse(LunchboxDeliveryPolicy.canFullyInsert(1, 1));
        assertFalse(LunchboxDeliveryPolicy.canFullyInsert(2, 1));
    }

    @Test
    void acceptsWhenSimulateLeftoverEmpty() {
        assertTrue(LunchboxDeliveryPolicy.canFullyInsert(1, 0));
        assertTrue(LunchboxDeliveryPolicy.wasFullyInserted(1, 0));
    }

    @Test
    void refusesEmptyFood() {
        assertFalse(LunchboxDeliveryPolicy.canFullyInsert(0, 0));
    }
}
