package com.touhoumaid.eatproperly.trinket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoonAppleMathTest {
    @Test
    void noElapsedTicksRestoresNothing() {
        MoonAppleMath.Result result = MoonAppleMath.accrue(0.0, 0);
        assertEquals(0, result.wholePoints());
        assertEquals(0.0, result.leftoverProgress(), 1e-9);
    }

    @Test
    void oneGameHourAccruesLessThanOnePoint() {
        // 1000 ticks (one game-hour) * (20/24000 per tick) = 5/6 ~= 0.833, no whole point yet.
        MoonAppleMath.Result result = MoonAppleMath.accrue(0.0, 1000);
        assertEquals(0, result.wholePoints());
        assertEquals(20.0 / 24.0, result.leftoverProgress(), 1e-9);
    }

    @Test
    void leftoverProgressCarriesForwardAcrossCalls() {
        MoonAppleMath.Result first = MoonAppleMath.accrue(0.0, 1000);
        MoonAppleMath.Result second = MoonAppleMath.accrue(first.leftoverProgress(), 1000);
        // After 2000 ticks total: 2000 * 20/24000 = 1.6667 -> 1 whole point restored so far.
        assertEquals(1, second.wholePoints());
    }

    @Test
    void fullGameDayRestoresAboutTwentyPointsTotal() {
        double progress = 0.0;
        int totalRestored = 0;
        for (int hour = 0; hour < 24; hour++) {
            MoonAppleMath.Result result = MoonAppleMath.accrue(progress, 1000);
            totalRestored += result.wholePoints();
            progress = result.leftoverProgress();
        }
        // Floors each step so the running total can be 19 or 20 depending on rounding boundaries,
        // but must land within 1 point of the target and never exceed it.
        assertTrue(totalRestored >= 19 && totalRestored <= 20,
                "expected ~20 total restored, got " + totalRestored);
    }

    @Test
    void negativeElapsedTicksAreTreatedAsZero() {
        MoonAppleMath.Result result = MoonAppleMath.accrue(0.5, -1000);
        assertEquals(0, result.wholePoints());
        assertEquals(0.5, result.leftoverProgress(), 1e-9);
    }
}
