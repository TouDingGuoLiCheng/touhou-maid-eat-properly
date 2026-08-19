package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodPermitSwapPolicyTest {
    @Test
    void dumpTierOrdersNonPoolFoodThenOtherThenTool() {
        assertTrue(FoodPermitSwapPolicy.compareDumpPriority(
                FoodPermitSwapPolicy.DumpTier.NON_POOL_FOOD,
                FoodPermitSwapPolicy.DumpTier.OTHER_NON_TOOL) < 0);
        assertTrue(FoodPermitSwapPolicy.compareDumpPriority(
                FoodPermitSwapPolicy.DumpTier.OTHER_NON_TOOL,
                FoodPermitSwapPolicy.DumpTier.TOOL) < 0);
    }

    @Test
    void classifyMatchesAgreedRules() {
        assertEquals(FoodPermitSwapPolicy.DumpTier.NON_POOL_FOOD,
                FoodPermitSwapPolicy.classifyDump(true, false, false));
        assertEquals(FoodPermitSwapPolicy.DumpTier.OTHER_NON_TOOL,
                FoodPermitSwapPolicy.classifyDump(true, true, false));
        assertEquals(FoodPermitSwapPolicy.DumpTier.OTHER_NON_TOOL,
                FoodPermitSwapPolicy.classifyDump(false, false, false));
        assertEquals(FoodPermitSwapPolicy.DumpTier.TOOL,
                FoodPermitSwapPolicy.classifyDump(false, false, true));
    }

    @Test
    void takeCountUsesMaxTakeUnlessWholeStackRequired() {
        assertEquals(5, FoodPermitSwapPolicy.decideTakeCount(64, 5, false));
        assertEquals(64, FoodPermitSwapPolicy.decideTakeCount(64, 5, true));
        assertEquals(3, FoodPermitSwapPolicy.decideTakeCount(3, 5, false));
    }

    @Test
    void wholeStackNeededWhenDumpDoesNotFitWithoutClearingFoodSlot() {
        assertTrue(FoodPermitSwapPolicy.needWholeStackToFitDump(false));
        assertFalse(FoodPermitSwapPolicy.needWholeStackToFitDump(true));
    }
}
