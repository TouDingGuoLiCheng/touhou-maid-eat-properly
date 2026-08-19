package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaidMealHungerPolicyTest {

    @Test
    void workOrHomeMealAllowedOnlyBelowEatGate() {
        assertTrue(MaidMealHungerPolicy.canAttemptWorkOrHomeMeal(15, 16));
        assertFalse(MaidMealHungerPolicy.canAttemptWorkOrHomeMeal(16, 16));
        assertFalse(MaidMealHungerPolicy.canAttemptWorkOrHomeMeal(20, 16));
        assertTrue(MaidMealHungerPolicy.canAttemptWorkOrHomeMeal(17, 18));
        assertFalse(MaidMealHungerPolicy.canAttemptWorkOrHomeMeal(18, 18));
    }

    @Test
    void fakeSkipWhenAtOrAboveGate() {
        assertFalse(MaidMealHungerPolicy.shouldFakeSkipMeal(15, 16));
        assertTrue(MaidMealHungerPolicy.shouldFakeSkipMeal(16, 16));
        assertTrue(MaidMealHungerPolicy.shouldFakeSkipMeal(20, 16));
    }

    @Test
    void bingeBypassesCooldownOnlyBelowTen() {
        assertTrue(MaidMealHungerPolicy.shouldBypassMealCooldown(0));
        assertTrue(MaidMealHungerPolicy.shouldBypassMealCooldown(9));
        assertFalse(MaidMealHungerPolicy.shouldBypassMealCooldown(10));
        assertFalse(MaidMealHungerPolicy.shouldBypassMealCooldown(15));
    }

    @Test
    void bingeUsesConfiguredStopLine() {
        assertTrue(MaidMealHungerPolicy.shouldBypassMealCooldown(9, 10));
        assertFalse(MaidMealHungerPolicy.shouldBypassMealCooldown(10, 10));
    }

    @Test
    void bingeBypassRequiresEdibleFood() {
        assertTrue(MaidMealHungerPolicy.shouldBypassMealCooldown(8, 10, true));
        assertFalse(MaidMealHungerPolicy.shouldBypassMealCooldown(8, 10, false));
        assertFalse(MaidMealHungerPolicy.shouldBypassMealCooldown(10, 10, true));
        assertFalse(MaidMealHungerPolicy.shouldBypassMealCooldown(10, 10, false));
    }

    @Test
    void mealFavorTypesAreRecognized() {
        assertTrue(MaidMealHungerPolicy.isMealFavorType("WorkMeal"));
        assertTrue(MaidMealHungerPolicy.isMealFavorType("HomeMeal"));
        assertFalse(MaidMealHungerPolicy.isMealFavorType("Sleep"));
        assertFalse(MaidMealHungerPolicy.isMealFavorType(null));
    }

    @Test
    void mealZoneSplitsBingeNormalAndFakeSkip() {
        assertEquals(MaidMealHungerPolicy.MealZone.BINGE, MaidMealHungerPolicy.mealZone(0, 10, 16));
        assertEquals(MaidMealHungerPolicy.MealZone.BINGE, MaidMealHungerPolicy.mealZone(9, 10, 16));
        assertEquals(MaidMealHungerPolicy.MealZone.NORMAL_CD, MaidMealHungerPolicy.mealZone(10, 10, 16));
        assertEquals(MaidMealHungerPolicy.MealZone.NORMAL_CD, MaidMealHungerPolicy.mealZone(15, 10, 16));
        assertEquals(MaidMealHungerPolicy.MealZone.FAKE_SKIP, MaidMealHungerPolicy.mealZone(16, 10, 16));
        assertEquals(MaidMealHungerPolicy.MealZone.FAKE_SKIP, MaidMealHungerPolicy.mealZone(20, 10, 15));
    }
}
