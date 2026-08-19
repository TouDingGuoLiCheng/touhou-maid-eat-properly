package com.touhoumaid.eatproperly.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HappinessTimingTest {
    @Test
    void isTenMinutesLong() {
        assertEquals(12_000, HappinessTiming.DURATION_TICKS);
    }

    @Test
    void restoresHungerEveryTwentyTicks() {
        assertTrue(HappinessTiming.isHungerRestoreTick(20));
        assertTrue(HappinessTiming.isHungerRestoreTick(40));
        assertTrue(HappinessTiming.isHungerRestoreTick(HappinessTiming.DURATION_TICKS));
    }

    @Test
    void doesNotRestoreOnOffTicks() {
        assertFalse(HappinessTiming.isHungerRestoreTick(21));
        assertFalse(HappinessTiming.isHungerRestoreTick(19));
        assertFalse(HappinessTiming.isHungerRestoreTick(1));
    }

    @Test
    void doesNotRestoreAtOrBelowZeroRemainingDuration() {
        assertFalse(HappinessTiming.isHungerRestoreTick(0));
        assertFalse(HappinessTiming.isHungerRestoreTick(-20));
    }

    @Test
    void heartParticlesEveryFifteenTicks() {
        assertTrue(HappinessTiming.isHeartParticleTick(15));
        assertTrue(HappinessTiming.isHeartParticleTick(30));
        assertFalse(HappinessTiming.isHeartParticleTick(14));
        assertFalse(HappinessTiming.isHeartParticleTick(0));
    }
}
