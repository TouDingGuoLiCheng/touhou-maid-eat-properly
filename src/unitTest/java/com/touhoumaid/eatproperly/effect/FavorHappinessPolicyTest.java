package com.touhoumaid.eatproperly.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FavorHappinessPolicyTest {
    @Test
    void refreshWhenEligibleAndNoEffect() {
        assertTrue(FavorHappinessPolicy.shouldRefresh(true, 0));
    }

    @Test
    void refreshWhenEligibleAndNearExpiry() {
        assertTrue(FavorHappinessPolicy.shouldRefresh(true, 20));
        assertTrue(FavorHappinessPolicy.shouldRefresh(true, 1));
    }

    @Test
    void noRefreshWhenDurationAboveThreshold() {
        assertFalse(FavorHappinessPolicy.shouldRefresh(true, 21));
        assertFalse(FavorHappinessPolicy.shouldRefresh(true, 3600));
    }

    @Test
    void neverRefreshWhenNotEligible() {
        assertFalse(FavorHappinessPolicy.shouldRefresh(false, 0));
        assertFalse(FavorHappinessPolicy.shouldRefresh(false, 5));
    }
}
