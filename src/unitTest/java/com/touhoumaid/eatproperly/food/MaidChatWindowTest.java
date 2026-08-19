package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TLM ScheduleButton windows:
 * DAY sleep 22:00–06:00 → chat 06:00–22:00
 * NIGHT sleep 06:00–14:00 → chat 14:00–06:00
 * ALL no sleep → chat always
 */
class MaidChatWindowTest {

    // dayTime-of-day: 06:00=0, 14:00=8000, 22:00=16000, 00:00=18000

    @Test
    void dayShiftChatIsSixToTwentyTwo() {
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.DAY, 0)); // 06:00
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.DAY, 6000)); // 12:00
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.DAY, 15999)); // ~21:59
        assertFalse(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.DAY, 16000)); // 22:00
        assertFalse(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.DAY, 18000)); // 00:00
        assertFalse(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.DAY, 23000)); // ~05:00
    }

    @Test
    void nightShiftChatSkipsMorningSleep() {
        assertFalse(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.NIGHT, 0)); // 06:00 sleep
        assertFalse(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.NIGHT, 4000)); // 10:00 sleep
        assertFalse(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.NIGHT, 7999)); // ~13:59
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.NIGHT, 8000)); // 14:00
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.NIGHT, 12000)); // 18:00
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.NIGHT, 18000)); // 00:00
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.NIGHT, 23999)); // ~05:59
    }

    @Test
    void allShiftAlwaysAllowsChat() {
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.ALL, 0));
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.ALL, 12000));
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.ALL, 18000));
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.ALL, 23999));
    }

    @Test
    void dayDeferredFromSleepLandsInNextDayWindow() {
        long now = 17000; // ~23:00 sleep
        long at = MaidChatWindow.nextRandomDeliveryDayTime(MaidChatWindow.Shift.DAY, now, 100);
        assertEquals(24000 + 100, at);
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.DAY, at));
        assertTrue(at > now);
    }

    @Test
    void nightDeferredFromSleepLandsLaterToday() {
        long now = 1000; // ~07:00 sleep
        long at = MaidChatWindow.nextRandomDeliveryDayTime(MaidChatWindow.Shift.NIGHT, now, 0);
        assertEquals(8000, at); // 14:00
        assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.NIGHT, at));
        assertTrue(at > now);
    }

    @Test
    void nightDeferredOffsetsDoNotCollide() {
        long now = 2000; // sleep
        Set<Long> times = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            times.add(MaidChatWindow.nextRandomDeliveryDayTime(MaidChatWindow.Shift.NIGHT, now, i * 100));
        }
        assertEquals(20, times.size());
        for (long at : times) {
            assertTrue(MaidChatWindow.canPublicChat(MaidChatWindow.Shift.NIGHT, at));
        }
    }

    @Test
    void dayWindowLengthIsSixteenHours() {
        assertEquals(16000, MaidChatWindow.windowLength(MaidChatWindow.Shift.DAY));
        assertEquals(16000, MaidChatWindow.windowLength(MaidChatWindow.Shift.NIGHT));
        assertEquals(24000, MaidChatWindow.windowLength(MaidChatWindow.Shift.ALL));
    }
}
