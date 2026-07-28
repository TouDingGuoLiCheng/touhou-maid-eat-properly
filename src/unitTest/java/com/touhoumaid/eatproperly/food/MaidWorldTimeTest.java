package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaidWorldTimeTest {

    @Test
    void tlmHourFormulaMatchesScheduleButton() {
        // dayTime 0 → 06:00; 2000 → 08:00; 6000 → 12:00; 14000 → 20:00
        assertEquals(6, MaidWorldTime.hourOfDay(0));
        assertEquals(8, MaidWorldTime.hourOfDay(2000));
        assertEquals(12, MaidWorldTime.hourOfDay(6000));
        assertEquals(20, MaidWorldTime.hourOfDay(14000));
        assertEquals(0, MaidWorldTime.hourOfDay(18000)); // 00:00
    }

    @Test
    void chatWindowIsEightToTwenty() {
        assertFalse(MaidWorldTime.isChatWindow(1999));
        assertTrue(MaidWorldTime.isChatWindow(2000));
        assertTrue(MaidWorldTime.isChatWindow(6000));
        assertTrue(MaidWorldTime.isChatWindow(13999));
        assertFalse(MaidWorldTime.isChatWindow(14000));
        assertFalse(MaidWorldTime.isChatWindow(18000));
        assertFalse(MaidWorldTime.isChatWindow(0));
    }

    @Test
    void chatWindowUsesAbsoluteDayTimeModulo() {
        assertTrue(MaidWorldTime.isChatWindow(24000 + 6000));
        assertFalse(MaidWorldTime.isChatWindow(24000 + 18000));
    }

    @Test
    void deferredBeforeDawnLandsTodayInWindow() {
        long now = 500; // ~06:30
        long at = MaidWorldTime.nextRandomDeliveryDayTime(now, 0);
        assertEquals(2000, at);
        assertTrue(MaidWorldTime.isChatWindow(at));
        assertTrue(at > now);
    }

    @Test
    void deferredAfterEveningLandsTomorrowInWindow() {
        long now = 15000; // ~21:00
        long at = MaidWorldTime.nextRandomDeliveryDayTime(now, 100);
        assertEquals(24000 + 2000 + 100, at);
        assertTrue(MaidWorldTime.isChatWindow(at));
        assertTrue(at > now);
    }

    @Test
    void deferredInsideWindowDoesNotGoBackward() {
        long now = 5000; // ~11:00
        long later = MaidWorldTime.nextRandomDeliveryDayTime(now, 4000); // target tod 6000
        assertEquals(6000, later);
        long earlierOffset = MaidWorldTime.nextRandomDeliveryDayTime(now, 0); // target tod 2000
        assertEquals(24000 + 2000, earlierOffset);
    }

    @Test
    void differentOffsetsDoNotShareSameAt() {
        long now = 16000;
        Set<Long> times = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            times.add(MaidWorldTime.nextRandomDeliveryDayTime(now, i * 100));
        }
        assertEquals(20, times.size());
        for (long at : times) {
            assertTrue(MaidWorldTime.isChatWindow(at));
        }
    }

    @Test
    void timeSetStyleDayTimeAlignsWithGuiHours() {
        // /time set values are dayTime-of-day; hours match TLM ScheduleButton.
        assertEquals(19, MaidWorldTime.hourOfDay(13000)); // 19:00 — still inside window
        assertTrue(MaidWorldTime.isChatWindow(13000));
        assertEquals(0, MaidWorldTime.hourOfDay(18000)); // 00:00
        assertFalse(MaidWorldTime.isChatWindow(18000));
    }
}
