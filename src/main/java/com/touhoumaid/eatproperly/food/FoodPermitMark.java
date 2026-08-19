package com.touhoumaid.eatproperly.food;

/**
 * One marked container position stored on a Food Permit item.
 */
public record FoodPermitMark(String dimension, int x, int y, int z) {
    public FoodPermitMark {
        if (dimension == null || dimension.isEmpty()) {
            throw new IllegalArgumentException("dimension");
        }
    }
}
