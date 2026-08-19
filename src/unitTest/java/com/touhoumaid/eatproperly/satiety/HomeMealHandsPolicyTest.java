package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeMealHandsPolicyTest {
    @Test
    void neverBlocksPicnicEatWhenHandCannotStowToBackpack() {
        assertFalse(HomeMealHandsPolicy.shouldAbortForHandFull(true));
        assertFalse(HomeMealHandsPolicy.shouldAbortForHandFull(false));
    }

    @Test
    void simulateLeftoverIsClearedSoVanillaGatePasses() {
        assertTrue(HomeMealHandsPolicy.shouldClearSimulateLeftover(true));
        assertFalse(HomeMealHandsPolicy.shouldClearSimulateLeftover(false));
    }
}
