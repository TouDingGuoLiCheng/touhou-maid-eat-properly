package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AngerAttractionPolicyTest {
    @Test
    void blocksAttractionWhileAngry() {
        assertFalse(AngerAttractionPolicy.shouldAllowAttraction(true));
        assertTrue(AngerAttractionPolicy.shouldAllowAttraction(false));
    }
}
