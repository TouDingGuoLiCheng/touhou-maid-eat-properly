package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdleMealPolicyTest {
    @Test
    void idleAllowsEatWhenAwakeAndMoving() {
        assertTrue(IdleMealPolicy.shouldAttemptIdleEat("idle", false, true));
    }

    @Test
    void workDoesNotDoubleDrive() {
        assertFalse(IdleMealPolicy.shouldAttemptIdleEat("work", false, true));
    }

    @Test
    void sleepAndRestBlocked() {
        assertFalse(IdleMealPolicy.shouldAttemptIdleEat("idle", true, true));
        assertFalse(IdleMealPolicy.shouldAttemptIdleEat("rest", false, true));
        assertFalse(IdleMealPolicy.shouldAttemptIdleEat("idle", false, false));
    }

    @Test
    void satietyGateMatchesWorkMeal() {
        assertTrue(IdleMealPolicy.satietyAllows(15, 16));
        assertFalse(IdleMealPolicy.satietyAllows(16, 16));
    }
}
