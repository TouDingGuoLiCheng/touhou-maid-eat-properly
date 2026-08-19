package com.touhoumaid.eatproperly.snackraid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnackRaidPhaseTest {
    @Test
    void roundTripsPhaseIds() {
        for (SnackRaidPhase phase : SnackRaidPhase.values()) {
            assertEquals(phase, SnackRaidPhase.fromId(phase.id()));
        }
        assertEquals(SnackRaidPhase.IDLE, SnackRaidPhase.fromId(999));
    }
}
