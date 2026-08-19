package com.touhoumaid.eatproperly.satiety;

/**
 * Pure math mirroring {@link net.minecraft.world.food.FoodData} eat / exhaustion / drain rules.
 */
public final class MaidFoodMath {
    public static final int FOOD_MAX = 20;
    public static final float EXHAUSTION_DROP = 4.0F;

    private MaidFoodMath() {
    }

    /** Same formula as {@code FoodData.eat(int, float)}. */
    public static EatResult eat(int foodLevel, float saturationLevel, int nutrition, float saturationModifier) {
        if (nutrition <= 0) {
            return new EatResult(foodLevel, clampSaturation(saturationLevel, foodLevel));
        }
        int newFood = Math.min(nutrition + foodLevel, FOOD_MAX);
        float addedSat = nutrition * saturationModifier * 2.0F;
        float newSat = Math.min(newFood, saturationLevel + addedSat);
        return new EatResult(newFood, clampSaturation(newSat, newFood));
    }

    /**
     * One exhaustion settlement step when {@code exhaustion > 4}: drop 4 exhaustion, then
     * saturation -1 or food -1.
     */
    public static FoodState settleExhaustionOnce(FoodState state) {
        if (state.exhaustion() <= EXHAUSTION_DROP) {
            return state;
        }
        float exhaustion = state.exhaustion() - EXHAUSTION_DROP;
        int food = state.foodLevel();
        float saturation = state.saturationLevel();
        if (saturation > 0.0F) {
            saturation = Math.max(saturation - 1.0F, 0.0F);
        } else if (food > 0) {
            food = Math.max(food - 1, 0);
        }
        saturation = clampSaturation(saturation, food);
        return new FoodState(food, saturation, exhaustion);
    }

    /** Daily drain: saturation first, then food. */
    public static EatResult drainSaturationFirst(int foodLevel, float saturationLevel, int points) {
        if (points <= 0) {
            return new EatResult(foodLevel, clampSaturation(saturationLevel, foodLevel));
        }
        int remaining = points;
        int satDrain = Math.min((int) Math.floor(saturationLevel), remaining);
        float newSat = saturationLevel - satDrain;
        remaining -= satDrain;
        int newFood = Math.max(0, foodLevel - remaining);
        newSat = clampSaturation(newSat, newFood);
        return new EatResult(newFood, newSat);
    }

    public static float clampSaturation(float saturation, int foodLevel) {
        if (saturation < 0.0F) {
            return 0.0F;
        }
        return Math.min(saturation, foodLevel);
    }

    public record EatResult(int foodLevel, float saturationLevel) {
    }

    public record FoodState(int foodLevel, float saturationLevel, float exhaustion) {
    }
}
