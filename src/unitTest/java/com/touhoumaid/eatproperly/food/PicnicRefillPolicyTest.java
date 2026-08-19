package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PicnicRefillPolicyTest {
    @Test
    void doesNothingWhenNotRestingOrMatHasFood() {
        assertEquals(PicnicRefillPolicy.Action.NONE,
                PicnicRefillPolicy.decide(false, false, true, true));
        assertEquals(PicnicRefillPolicy.Action.NONE,
                PicnicRefillPolicy.decide(true, true, true, true));
    }

    @Test
    void flushesInPlaceWhenCarryingFood() {
        assertEquals(PicnicRefillPolicy.Action.FLUSH_IN_PLACE,
                PicnicRefillPolicy.decide(true, false, true, false));
    }

    @Test
    void leavesToFetchOnlyWhenNoFoodButTargetReachable() {
        assertEquals(PicnicRefillPolicy.Action.LEAVE_TO_FETCH,
                PicnicRefillPolicy.decide(true, false, false, true));
        assertEquals(PicnicRefillPolicy.Action.NONE,
                PicnicRefillPolicy.decide(true, false, false, false));
    }

    @Test
    void clearsSeekWhenBackOnOriginalMatTimedOutOrHomeGone() {
        // Carrying food alone must NOT clear seek — she still needs to return to the original mat.
        assertFalse(PicnicRefillPolicy.shouldClearSeek(false, false, 100, 0, 1200));
        assertTrue(PicnicRefillPolicy.shouldClearSeek(true, false, 100, 0, 1200));
        assertTrue(PicnicRefillPolicy.shouldClearSeek(false, true, 100, 0, 1200));
        assertTrue(PicnicRefillPolicy.shouldClearSeek(false, false, 1200, 0, 1200));
        assertFalse(PicnicRefillPolicy.shouldClearSeek(false, false, 1199, 0, 1200));
    }

    @Test
    void suppressesFindHomeMealOnlyWhileSeekingWithoutCarriedFood() {
        assertTrue(PicnicRefillPolicy.shouldSuppressFindHomeMeal(true, false));
        assertFalse(PicnicRefillPolicy.shouldSuppressFindHomeMeal(true, true));
        assertFalse(PicnicRefillPolicy.shouldSuppressFindHomeMeal(false, false));
        assertFalse(PicnicRefillPolicy.shouldSuppressFindHomeMeal(false, true));
    }

    @Test
    void forcesOriginalMatWhenSeekingWithCarriedFood() {
        assertTrue(PicnicRefillPolicy.shouldForceOriginalMat(true, true, true));
        assertFalse(PicnicRefillPolicy.shouldForceOriginalMat(true, false, true));
        assertFalse(PicnicRefillPolicy.shouldForceOriginalMat(true, true, false));
        assertFalse(PicnicRefillPolicy.shouldForceOriginalMat(false, true, true));
    }

    @Test
    void defersRefillWhileMaidIsUsingFood() {
        // Last picnic food is extracted into the hand and startUsingItem runs; mat is empty.
        // Flushing that hand back onto the mat causes "ghost eat" (satiety applied, food stays).
        assertTrue(PicnicRefillPolicy.shouldDeferWhileUsingItem(true));
        assertFalse(PicnicRefillPolicy.shouldDeferWhileUsingItem(false));
    }

    @Test
    void seekingPicnicRefillBypassesFoodPermitSatietyGate() {
        assertTrue(PicnicRefillPolicy.shouldBypassFoodPermitSatiety(true));
        assertFalse(PicnicRefillPolicy.shouldBypassFoodPermitSatiety(false));
    }

    @Test
    void satietyGateAllowsStartWhenSeekingOrBelowThreshold() {
        assertTrue(PicnicRefillPolicy.allowsFoodPermitStart(true, 20, 15));
        assertTrue(PicnicRefillPolicy.allowsFoodPermitStart(false, 10, 15));
        assertFalse(PicnicRefillPolicy.allowsFoodPermitStart(false, 15, 15));
        assertFalse(PicnicRefillPolicy.allowsFoodPermitStart(false, 20, 15));
    }

    @Test
    void seekingDoesNotAbortPermitFetchForHighSatiety() {
        assertFalse(PicnicRefillPolicy.shouldAbortPermitFetchForSatiety(true, true));
        assertTrue(PicnicRefillPolicy.shouldAbortPermitFetchForSatiety(false, true));
        assertFalse(PicnicRefillPolicy.shouldAbortPermitFetchForSatiety(false, false));
    }

    @Test
    void seekingBypassesFoodPermitCooldown() {
        assertTrue(PicnicRefillPolicy.shouldBypassFoodPermitCooldown(true));
        assertFalse(PicnicRefillPolicy.shouldBypassFoodPermitCooldown(false));
    }
}
