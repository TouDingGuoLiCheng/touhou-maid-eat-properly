package com.touhoumaid.eatproperly.food;

/**
 * Pure rule: while angry at out-of-pool gifts, do not be attracted to owner-held food.
 */
public final class AngerAttractionPolicy {
    private AngerAttractionPolicy() {
    }

    public static boolean shouldAllowAttraction(boolean angry) {
        return !angry;
    }
}
