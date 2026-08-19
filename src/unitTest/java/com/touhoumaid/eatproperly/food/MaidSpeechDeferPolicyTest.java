package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaidSpeechDeferPolicyTest {

    @Test
    void deferredOnlyWhenNotForcedAndOutsideWindow() {
        assertFalse(MaidSpeechDeferPolicy.shouldDeferPublicChat(true, false));
        assertFalse(MaidSpeechDeferPolicy.shouldDeferPublicChat(true, true));
        assertFalse(MaidSpeechDeferPolicy.shouldDeferPublicChat(false, true));
        assertTrue(MaidSpeechDeferPolicy.shouldDeferPublicChat(false, false));
    }
}
