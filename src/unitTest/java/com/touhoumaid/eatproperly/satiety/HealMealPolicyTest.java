package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealMealPolicyTest {

    @Test
    void sharesWorkHomeSatietyGate() {
        assertTrue(HealMealPolicy.satietyAllows(15, 16));
        assertFalse(HealMealPolicy.satietyAllows(16, 16));
    }

    @Test
    void cooldownBlocksUntilElapsed() {
        assertTrue(HealMealPolicy.cooldownReady(-1L, 100L, 3600));
        assertFalse(HealMealPolicy.cooldownReady(0L, 3599L, 3600));
        assertTrue(HealMealPolicy.cooldownReady(0L, 3600L, 3600));
        assertEquals(1200L, HealMealPolicy.remainingCooldownTicks(0L, 2400L, 3600));
    }

    @Test
    void allowsEatRequiresGateAndCooldown() {
        assertTrue(HealMealPolicy.allowsEat(10, 16, -1L, 0L, 3600));
        assertFalse(HealMealPolicy.allowsEat(16, 16, -1L, 0L, 3600));
        assertFalse(HealMealPolicy.allowsEat(10, 16, 0L, 100L, 3600));
    }
}
