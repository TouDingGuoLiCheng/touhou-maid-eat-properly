package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaidFoodReceivePolicyTest {
    @Test
    void emptyHandAllowsDeliveryEvenWhenBackpackFull() {
        assertTrue(MaidFoodReceivePolicy.canFullyReceive(true, 1, 1));
        assertFalse(MaidFoodReceivePolicy.shouldRejectDelivery(false, true, 1, 1));
    }

    @Test
    void bothHandsFullRequiresBackpackSpace() {
        assertTrue(MaidFoodReceivePolicy.canFullyReceive(false, 1, 0));
        assertFalse(MaidFoodReceivePolicy.canFullyReceive(false, 1, 1));
    }

    @Test
    void rejectOnlyWhenHandsAndBackpackFull() {
        assertTrue(MaidFoodReceivePolicy.shouldRejectDelivery(false, false, 1, 1));
        assertFalse(MaidFoodReceivePolicy.shouldRejectDelivery(false, false, 1, 0));
        assertFalse(MaidFoodReceivePolicy.shouldRejectDelivery(true, false, 1, 1));
        assertFalse(MaidFoodReceivePolicy.shouldRejectDelivery(false, true, 1, 1));
    }

    @Test
    void bothHandsOccupiedDetectsFullHands() {
        assertTrue(MaidFoodReceivePolicy.bothHandsOccupied(false, false));
        assertFalse(MaidFoodReceivePolicy.bothHandsOccupied(true, false));
        assertFalse(MaidFoodReceivePolicy.bothHandsOccupied(false, true));
    }
}
