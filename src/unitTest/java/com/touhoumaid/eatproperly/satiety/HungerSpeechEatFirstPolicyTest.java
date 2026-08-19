package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HungerSpeechEatFirstPolicyTest {
    @Test
    void prefersEatWhenBackpackHasFood() {
        assertEquals(HungerSpeechEatFirstPolicy.Decision.EAT_FIRST,
                HungerSpeechEatFirstPolicy.decide(true));
    }

    @Test
    void speaksWhenNoBackpackFood() {
        assertEquals(HungerSpeechEatFirstPolicy.Decision.SPEAK,
                HungerSpeechEatFirstPolicy.decide(false));
    }

    @Test
    void successfulEatResetsHungerCooldown() {
        assertTrue(HungerSpeechEatFirstPolicy.shouldResetHungerElapsedAfterEat(true));
        assertFalse(HungerSpeechEatFirstPolicy.shouldSpeakAfterFailedEat(true));
    }

    @Test
    void failedEatFallsThroughToSpeak() {
        assertFalse(HungerSpeechEatFirstPolicy.shouldResetHungerElapsedAfterEat(false));
        assertTrue(HungerSpeechEatFirstPolicy.shouldSpeakAfterFailedEat(false));
    }
}
