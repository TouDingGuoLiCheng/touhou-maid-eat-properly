package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LowSatietyBegPolicyTest {
    @Test
    void satietyMustBeStrictlyBelowEight() {
        assertTrue(LowSatietyBegPolicy.satietyAllows(0));
        assertTrue(LowSatietyBegPolicy.satietyAllows(7));
        assertFalse(LowSatietyBegPolicy.satietyAllows(8));
        assertFalse(LowSatietyBegPolicy.satietyAllows(20));
    }

    @Test
    void ownerRangeIsSixInclusive() {
        assertTrue(LowSatietyBegPolicy.ownerInRange(0));
        assertTrue(LowSatietyBegPolicy.ownerInRange(6.0));
        assertFalse(LowSatietyBegPolicy.ownerInRange(6.01));
    }

    @Test
    void givePathOnlyWhileBeggingWithPoolFood() {
        assertEquals(LowSatietyBegPolicy.GiveAction.REJECT,
                LowSatietyBegPolicy.decideGive(false, true, true));
        assertEquals(LowSatietyBegPolicy.GiveAction.REJECT,
                LowSatietyBegPolicy.decideGive(true, false, false));
        assertEquals(LowSatietyBegPolicy.GiveAction.COMPLETE_REQUEST,
                LowSatietyBegPolicy.decideGive(true, true, true));
        assertEquals(LowSatietyBegPolicy.GiveAction.STOW_ONLY,
                LowSatietyBegPolicy.decideGive(true, true, false));
    }
}
