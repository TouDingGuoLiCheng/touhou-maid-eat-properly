package com.touhoumaid.eatproperly.snackraid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnackRaidPathLockPolicyTest {
    @Test
    void expandsSmallRestrictionRadiusEnoughToSuppressFollowing() {
        assertEquals(64, SnackRaidPathLockPolicy.lockRadius(8));
    }

    @Test
    void neverShrinksAnExistingRestrictionRadius() {
        assertEquals(96, SnackRaidPathLockPolicy.lockRadius(96));
    }

    @Test
    void ownerMayStayWithinOriginalTeleportBoundary() {
        assertFalse(SnackRaidPathLockPolicy.ownerTooFar(9.9D, 8));
        assertFalse(SnackRaidPathLockPolicy.ownerTooFar(10.0D, 8));
    }

    @Test
    void ownerBeyondOriginalTeleportBoundaryEndsLock() {
        assertTrue(SnackRaidPathLockPolicy.ownerTooFar(10.1D, 8));
    }
}
