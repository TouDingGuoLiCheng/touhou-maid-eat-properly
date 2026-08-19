package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectivePoolEatPolicyTest {
    @Test
    void allowsOnlyEffectivePool() {
        assertTrue(EffectivePoolEatPolicy.allows(true));
        assertFalse(EffectivePoolEatPolicy.allows(false));
    }
}
