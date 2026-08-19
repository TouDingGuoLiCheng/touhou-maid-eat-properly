package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaidFoodMathTest {

    @Test
    void eatMatchesFoodDataFormula() {
        MaidFoodMath.EatResult bread = MaidFoodMath.eat(0, 0.0F, 5, 0.6F);
        assertEquals(5, bread.foodLevel());
        assertEquals(5.0F, bread.saturationLevel(), 0.001F);

        MaidFoodMath.EatResult topped = MaidFoodMath.eat(18, 12.0F, 5, 0.6F);
        assertEquals(20, topped.foodLevel());
        assertEquals(18.0F, topped.saturationLevel(), 0.001F);
    }

    @Test
    void drainSaturationFirstBeforeFood() {
        MaidFoodMath.EatResult result = MaidFoodMath.drainSaturationFirst(20, 6.0F, 4);
        assertEquals(20, result.foodLevel());
        assertEquals(2.0F, result.saturationLevel(), 0.001F);

        MaidFoodMath.EatResult spill = MaidFoodMath.drainSaturationFirst(20, 2.0F, 4);
        assertEquals(18, spill.foodLevel());
        assertEquals(0.0F, spill.saturationLevel(), 0.001F);
    }

    @Test
    void settleExhaustionConsumesSaturationBeforeFood() {
        MaidFoodMath.FoodState state = new MaidFoodMath.FoodState(20, 3.0F, 5.0F);
        MaidFoodMath.FoodState after = MaidFoodMath.settleExhaustionOnce(state);
        assertEquals(20, after.foodLevel());
        assertEquals(2.0F, after.saturationLevel(), 0.001F);
        assertEquals(1.0F, after.exhaustion(), 0.001F);

        MaidFoodMath.FoodState noSat = new MaidFoodMath.FoodState(20, 0.0F, 5.0F);
        MaidFoodMath.FoodState foodDown = MaidFoodMath.settleExhaustionOnce(noSat);
        assertEquals(19, foodDown.foodLevel());
        assertEquals(0.0F, foodDown.saturationLevel(), 0.001F);
    }
}
