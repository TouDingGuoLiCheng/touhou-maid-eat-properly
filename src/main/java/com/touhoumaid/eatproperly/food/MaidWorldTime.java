package com.touhoumaid.eatproperly.food;

/**
 * World-time helpers aligned with Touhou Little Maid's GUI clock
 * ({@code level.getDayTime()}, hour = {@code (tod / 1000 + 6) % 24}).
 * Public-chat windows live in {@link MaidChatWindow} (per-maid schedule).
 */
public final class MaidWorldTime {
    public static final int DAY_TICKS = 24000;
    /**
     * {@code dayTime % 24000} at TLM 24:00 / 00:00
     * ({@code hourOfDay} == 0). Satiety daily settlement uses this boundary.
     */
    public static final int MIDNIGHT_TOD = 18000;

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

    /**
     * Schedule-calendar day index that increments at TLM midnight ({@link #MIDNIGHT_TOD}).
     * Uses absolute {@code dayTime} (not {@code gameTime}).
     */
    public static long scheduleDay(long dayTime) {
        return Math.floorDiv(dayTime - MIDNIGHT_TOD, DAY_TICKS);
    }
}
