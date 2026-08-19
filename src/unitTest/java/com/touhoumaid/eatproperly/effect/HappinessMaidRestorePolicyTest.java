package com.touhoumaid.eatproperly.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HappinessMaidRestorePolicyTest {

    @Test
    void restoresOnSameCadenceAsEffectTick() {
        assertTrue(HappinessMaidRestorePolicy.shouldRestoreThisTick(true, 20));
        assertTrue(HappinessMaidRestorePolicy.shouldRestoreThisTick(true, 40));
        assertTrue(HappinessMaidRestorePolicy.shouldRestoreThisTick(true, 600));
    }

    @Test
    void skipsWhenEffectMissingOrOffCadence() {
        assertFalse(HappinessMaidRestorePolicy.shouldRestoreThisTick(false, 20));
        assertFalse(HappinessMaidRestorePolicy.shouldRestoreThisTick(true, 21));
        assertFalse(HappinessMaidRestorePolicy.shouldRestoreThisTick(true, 0));
    }
}
