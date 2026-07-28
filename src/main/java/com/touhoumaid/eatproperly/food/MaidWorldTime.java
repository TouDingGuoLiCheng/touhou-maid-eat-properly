package com.touhoumaid.eatproperly.food;

/**
 * World-time helpers aligned with Touhou Little Maid's GUI clock
 * ({@code level.getDayTime()}, hour = {@code (tod / 1000 + 6) % 24}).
 */
public final class MaidWorldTime {
    public static final int DAY_TICKS = 24000;

    /** 08:00 in TLM/dayTime-of-day ticks. */
    public static final int CHAT_WINDOW_START = 2000;
    /** 20:00 (exclusive end) in TLM/dayTime-of-day ticks. */
    public static final int CHAT_WINDOW_END = 14000;

    public static final int CHAT_WINDOW_LENGTH = CHAT_WINDOW_END - CHAT_WINDOW_START;

    private MaidWorldTime() {
    }

    /** {@code dayTime % 24000}, always in {@code [0, 24000)}. */
    public static long timeOfDay(long dayTime) {
        return Math.floorMod(dayTime, DAY_TICKS);
    }

    /** Hour of day 0–23, matching TLM {@code ScheduleButton}. */
    public static int hourOfDay(long dayTime) {
        return (int) ((timeOfDay(dayTime) / 1000 + 6) % 24);
    }

    /** Minute within the current hour, matching TLM {@code ScheduleButton}. */
    public static int minuteOfHour(long dayTime) {
        return (int) ((timeOfDay(dayTime) % 1000) * 60 / 1000);
    }

    /** True when local clock is in [08:00, 20:00). */
    public static boolean isChatWindow(long dayTime) {
        long tod = timeOfDay(dayTime);
        return tod >= CHAT_WINDOW_START && tod < CHAT_WINDOW_END;
    }

    /**
     * Absolute {@code dayTime} for a deferred public chat delivery.
     *
     * @param currentDayTime absolute {@code level.getDayTime()}
     * @param offsetInWindow offset within the chat window, in {@code [0, CHAT_WINDOW_LENGTH)}
     */
    public static long nextRandomDeliveryDayTime(long currentDayTime, int offsetInWindow) {
        int clamped = Math.floorMod(offsetInWindow, CHAT_WINDOW_LENGTH);
        long targetTod = CHAT_WINDOW_START + clamped;
        long tod = timeOfDay(currentDayTime);
        long dayStart = currentDayTime - tod;

        if (tod < CHAT_WINDOW_START) {
            // Before 08:00 → later today.
            return dayStart + targetTod;
        }
        if (tod >= CHAT_WINDOW_END) {
            // After 20:00 → tomorrow's window.
            return dayStart + DAY_TICKS + targetTod;
        }
        // Inside window (re-roll / edge): later today if possible, else tomorrow.
        if (targetTod > tod) {
            return dayStart + targetTod;
        }
        return dayStart + DAY_TICKS + targetTod;
    }
}
