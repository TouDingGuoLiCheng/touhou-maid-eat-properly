package com.touhoumaid.eatproperly.snackraid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnackRaidApproachPolicyTest {
    @Test
    void stealthRaidKeepsTheSneakySpeed() {
        assertEquals(SnackRaidApproachPolicy.STEALTH_SPEED,
                SnackRaidApproachPolicy.approachSpeed(false, 0.6D), 1.0E-6F);
    }

    @Test
    void permitFetchUsesConfiguredSpeed() {
        assertEquals(0.4F, SnackRaidApproachPolicy.approachSpeed(true, 0.4D), 1.0E-6F);
    }

    @Test
    void permitSpeedStaysFasterThanSneakingButNotASprint() {
        assertTrue(SnackRaidApproachPolicy.DEFAULT_PERMIT_SPEED > SnackRaidApproachPolicy.STEALTH_SPEED);
        assertTrue(SnackRaidApproachPolicy.DEFAULT_PERMIT_SPEED < SnackRaidApproachPolicy.MAX_PERMIT_SPEED);
        assertTrue(SnackRaidApproachPolicy.MAX_PERMIT_SPEED < SnackRaidFleePolicy.FLEE_SPEED);
    }

    @Test
    void configuredSpeedIsClampedIntoTheReadableRange() {
        assertEquals(SnackRaidApproachPolicy.STEALTH_SPEED,
                SnackRaidApproachPolicy.approachSpeed(true, 0.01D), 1.0E-6F);
        assertEquals(SnackRaidApproachPolicy.MAX_PERMIT_SPEED,
                SnackRaidApproachPolicy.approachSpeed(true, 5.0D), 1.0E-6F);
    }

    @Test
    void stealthLinesAreOnlyForActualSneaking() {
        assertTrue(SnackRaidApproachPolicy.shouldSpeakStealthApproach(false));
        assertFalse(SnackRaidApproachPolicy.shouldSpeakStealthApproach(true));
    }
}
