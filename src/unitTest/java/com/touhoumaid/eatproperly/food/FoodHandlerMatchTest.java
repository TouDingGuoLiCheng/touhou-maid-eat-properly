package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodHandlerMatchTest {
    @Test
    void matchesWhenAnySlotIdEqualsWanted() {
        assertTrue(FoodHandlerMatch.containsWanted(
                new String[]{"minecraft:bread", "minecraft:apple"},
                "minecraft:apple"));
    }

    @Test
    void rejectsWhenWantedMissingOrEmpty() {
        assertFalse(FoodHandlerMatch.containsWanted(new String[]{"minecraft:bread"}, "minecraft:apple"));
        assertFalse(FoodHandlerMatch.containsWanted(new String[]{"minecraft:apple"}, ""));
        assertFalse(FoodHandlerMatch.containsWanted(new String[]{}, "minecraft:apple"));
    }

    @Test
    void ignoresBlankSlotIds() {
        assertTrue(FoodHandlerMatch.containsWanted(new String[]{"", "minecraft:apple"}, "minecraft:apple"));
    }
}
