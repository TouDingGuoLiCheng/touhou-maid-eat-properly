package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MealFavorPolicyTest {
    @Test
    void favorIsFloorOfNutritionTimesOnePointFive() {
        assertEquals(0, MealFavorPolicy.pointsForNutrition(0));
        assertEquals(0, MealFavorPolicy.pointsForNutrition(-1));
        assertEquals(1, MealFavorPolicy.pointsForNutrition(1));
        assertEquals(7, MealFavorPolicy.pointsForNutrition(5));
        assertEquals(12, MealFavorPolicy.pointsForNutrition(8));
        assertEquals(15, MealFavorPolicy.pointsForNutrition(10));
    }

    @Test
    void positiveMealFavorAlwaysGrantsEvenIfCooldownBlocks() {
        assertTrue(MealFavorPolicy.shouldForceGrantMealFavor("WorkMeal", 7));
        assertTrue(MealFavorPolicy.shouldForceGrantMealFavor("HomeMeal", 1));
        assertFalse(MealFavorPolicy.shouldForceGrantMealFavor("WorkMeal", 0));
        assertFalse(MealFavorPolicy.shouldForceGrantMealFavor("Sleep", 8));
        assertFalse(MealFavorPolicy.shouldForceGrantMealFavor(null, 8));
    }
}
