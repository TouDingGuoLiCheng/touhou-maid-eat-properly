package com.touhoumaid.eatproperly.snackraid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnackRaidToggleTextsTest {
    @Test
    void localToggleDefaultsToAllowed() {
        assertTrue(SnackRaidToggleTexts.defaultAllowed());
    }

    @Test
    void feedbackKeyMatchesAllowedState() {
        assertEquals("gui.touhou_maid_eat_properly.snack_raid_allowed",
                SnackRaidToggleTexts.feedbackKey(true));
        assertEquals("gui.touhou_maid_eat_properly.snack_raid_disallowed",
                SnackRaidToggleTexts.feedbackKey(false));
    }
}
