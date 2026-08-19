package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodPoolSafetyTest {

    @Test
    void harmfulCategoryIsRejected() {
        assertTrue(FoodPoolSafety.isHarmfulCategoryName("HARMFUL"));
        assertFalse(FoodPoolSafety.isHarmfulCategoryName("BENEFICIAL"));
        assertFalse(FoodPoolSafety.isHarmfulCategoryName("NEUTRAL"));
    }
}
