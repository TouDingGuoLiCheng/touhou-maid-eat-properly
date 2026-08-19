package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodPermitFetchPolicyTest {
    @Test
    void workTimeFetchesBackpackOnlyWhenHandsAndBackpackAreEmpty() {
        assertEquals(FoodPermitFetchPolicy.FetchPlan.NONE,
                FoodPermitFetchPolicy.decide(false, false, true));
        assertEquals(new FoodPermitFetchPolicy.FetchPlan(FoodPermitFetchPolicy.Destination.BACKPACK, 5),
                FoodPermitFetchPolicy.decide(false, false, false));
    }

    @Test
    void restTimeRefillsPicnicFirstEvenIfBackpackStillHasFood() {
        assertEquals(new FoodPermitFetchPolicy.FetchPlan(FoodPermitFetchPolicy.Destination.PICNIC, 16),
                FoodPermitFetchPolicy.decide(true, false, true));
    }

    @Test
    void restTimeFallsBackToBackpackWhenPicnicAlreadyHasFood() {
        assertEquals(new FoodPermitFetchPolicy.FetchPlan(FoodPermitFetchPolicy.Destination.BACKPACK, 5),
                FoodPermitFetchPolicy.decide(true, true, false));
        assertEquals(FoodPermitFetchPolicy.FetchPlan.NONE,
                FoodPermitFetchPolicy.decide(true, true, true));
    }

    @Test
    void markedTargetBeatsSneakRaidFallback() {
        assertEquals(FoodPermitFetchPolicy.StartAction.FOOD_PERMIT,
                FoodPermitFetchPolicy.decideStart(true, true));
        assertEquals(FoodPermitFetchPolicy.StartAction.SNACK_RAID,
                FoodPermitFetchPolicy.decideStart(false, true));
        assertEquals(FoodPermitFetchPolicy.StartAction.NONE,
                FoodPermitFetchPolicy.decideStart(false, false));
    }

    @Test
    void cooldownUsesInclusiveReadyBoundary() {
        assertTrue(FoodPermitFetchPolicy.isOffCooldown(600, 0, 600));
        assertFalse(FoodPermitFetchPolicy.isOffCooldown(599, 0, 600));
        assertTrue(FoodPermitFetchPolicy.isOffCooldown(1000, 0, 0));
    }

    @Test
    void picnicRefillSeekUsesPicnicTakeEvenWhenNotSeated() {
        // Left the empty mat: task may no longer count as "resting", but seek intent still needs ≤16.
        assertEquals(new FoodPermitFetchPolicy.FetchPlan(FoodPermitFetchPolicy.Destination.PICNIC, 16),
                FoodPermitFetchPolicy.decide(false, false, false, true));
        assertEquals(FoodPermitFetchPolicy.FetchPlan.NONE,
                FoodPermitFetchPolicy.decide(false, false, true, true));
    }
}
