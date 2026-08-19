package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodPermitSpeechPolicyTest {

    @Test
    void permitFetchUsesPermitApproachNotStealth() {
        assertTrue(FoodPermitSpeechPolicy.speakPermitApproach(true));
        assertFalse(FoodPermitSpeechPolicy.speakPermitApproach(false));
    }

    @Test
    void permitSuccessOnlyWhenItemsMoved() {
        assertTrue(FoodPermitSpeechPolicy.speakPermitSuccess(true, 1));
        assertTrue(FoodPermitSpeechPolicy.speakPermitSuccess(true, 5));
        assertFalse(FoodPermitSpeechPolicy.speakPermitSuccess(true, 0));
        assertFalse(FoodPermitSpeechPolicy.speakPermitSuccess(false, 3));
    }

    @Test
    void permitTripBubblesDoNotRequireOwnerNearby() {
        assertFalse(FoodPermitSpeechPolicy.requireOwnerNearbyForBubble());
    }
}
