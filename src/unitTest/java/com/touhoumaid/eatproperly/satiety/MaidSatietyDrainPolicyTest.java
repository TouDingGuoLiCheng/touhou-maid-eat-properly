package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaidSatietyDrainPolicyTest {

    @Test
    void idleTaskSkipsDailyDrain() {
        assertFalse(MaidSatietyDrainPolicy.shouldApplyDailyDrain(true));
    }

    @Test
    void workingTaskAppliesDailyDrain() {
        assertTrue(MaidSatietyDrainPolicy.shouldApplyDailyDrain(false));
    }

    @Test
    void idleUidIsRecognized() {
        assertTrue(MaidSatietyDrainPolicy.isIdleTaskUid("touhou_little_maid:idle"));
        assertFalse(MaidSatietyDrainPolicy.isIdleTaskUid("touhou_little_maid:attack"));
        assertFalse(MaidSatietyDrainPolicy.isIdleTaskUid(null));
        assertFalse(MaidSatietyDrainPolicy.isIdleTaskUid(""));
    }
}
