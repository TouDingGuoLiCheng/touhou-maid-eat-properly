package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaidWorldTimeTest {

    @Test
    void tlmHourFormulaMatchesScheduleButton() {
        // dayTime 0 → 06:00; 2000 → 08:00; 6000 → 12:00; 14000 → 20:00; 16000 → 22:00
        assertEquals(6, MaidWorldTime.hourOfDay(0));
        assertEquals(8, MaidWorldTime.hourOfDay(2000));
        assertEquals(12, MaidWorldTime.hourOfDay(6000));
        assertEquals(20, MaidWorldTime.hourOfDay(14000));
        assertEquals(22, MaidWorldTime.hourOfDay(16000));
        assertEquals(0, MaidWorldTime.hourOfDay(18000)); // 00:00
    }

    @Test
    void timeOfDayWrapsAbsoluteDayTime() {
        assertEquals(6000, MaidWorldTime.timeOfDay(24000 + 6000));
        assertEquals(18000, MaidWorldTime.timeOfDay(24000 + 18000));
    }

    @Test
    void scheduleDayIncrementsAtTlmMidnight() {
        // TLM 24:00 / 00:00 is tod 18000; day index flips there (not at tod 0 / 06:00).
        assertEquals(-1L, MaidWorldTime.scheduleDay(0));       // 06:00 day before first midnight
        assertEquals(-1L, MaidWorldTime.scheduleDay(17999));   // just before 24:00
        assertEquals(0L, MaidWorldTime.scheduleDay(18000));    // 24:00 / 00:00
        assertEquals(0L, MaidWorldTime.scheduleDay(18000 + 6000)); // next 06:00
        assertEquals(1L, MaidWorldTime.scheduleDay(18000 + 24000));
        assertEquals(1L, MaidWorldTime.scheduleDay(18000 + 24000 + 100));
    }
}
