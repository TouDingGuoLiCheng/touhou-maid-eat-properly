package com.touhoumaid.eatproperly.milestone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimLevelGuardTest {
    @Test
    void acceptsMatchingPendingHeadLevel() {
        assertTrue(ClaimLevelGuard.matchesPendingHead(5, 5));
        assertTrue(ClaimLevelGuard.matchesPendingHead(60, 60));
    }

    @Test
    void rejectsStaleOrWrongLevelClicks() {
        assertFalse(ClaimLevelGuard.matchesPendingHead(5, 10));
        assertFalse(ClaimLevelGuard.matchesPendingHead(60, 5));
    }
}
