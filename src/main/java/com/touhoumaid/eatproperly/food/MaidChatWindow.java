package com.touhoumaid.eatproperly.food;

/**
 * Public-chat windows aligned with TLM maid schedules (ScheduleButton tooltips).
 * Chat is allowed during non-sleep activities (WORK + IDLE).
 *
 * <ul>
 *   <li>DAY: sleep 22:00–06:00 → chat [06:00, 22:00)</li>
 *   <li>NIGHT: sleep 06:00–14:00 → chat [14:00, 06:00)</li>
 *   <li>ALL: no sleep → chat always</li>
 * </ul>
 */
public final class MaidChatWindow {
    public enum Shift {
        DAY,
        NIGHT,
        ALL
    }

    /** DAY chat start 06:00 / NIGHT sleep end 06:00. */
    private static final int TOD_06 = 0;
    /** NIGHT chat start 14:00. */
    private static final int TOD_14 = 8000;
    /** DAY chat end / sleep start 22:00. */
    private static final int TOD_22 = 16000;

    private MaidChatWindow() {
    }

    public static boolean canPublicChat(Shift shift, long dayTime) {
        long tod = MaidWorldTime.timeOfDay(dayTime);
        return switch (shift) {
            case ALL -> true;
            case DAY -> tod >= TOD_06 && tod < TOD_22;
            case NIGHT -> tod >= TOD_14;
        };
    }

    public static int windowLength(Shift shift) {
        return switch (shift) {
            case ALL -> MaidWorldTime.DAY_TICKS;
            case DAY, NIGHT -> 16000;
        };
    }

    /**
     * Absolute {@code dayTime} for deferred public chat inside the given shift's awake window.
     *
     * @param offsetInWindow offset within the awake window, in {@code [0, windowLength)}
     */
    public static long nextRandomDeliveryDayTime(Shift shift, long currentDayTime, int offsetInWindow) {
        int length = windowLength(shift);
        int clamped = Math.floorMod(offsetInWindow, length);
        long tod = MaidWorldTime.timeOfDay(currentDayTime);
        long dayStart = currentDayTime - tod;

        return switch (shift) {
            case ALL -> nextInContiguousWindow(dayStart, tod, TOD_06, MaidWorldTime.DAY_TICKS, clamped);
            case DAY -> nextInContiguousWindow(dayStart, tod, TOD_06, TOD_22, clamped);
            case NIGHT -> nextInContiguousWindow(dayStart, tod, TOD_14, MaidWorldTime.DAY_TICKS, clamped);
        };
    }

    /**
     * Contiguous awake window {@code [windowStart, windowEnd)} within a day.
     * {@code offset} maps to {@code windowStart + offset}.
     */
    private static long nextInContiguousWindow(
            long dayStart, long tod, int windowStart, int windowEnd, int offset) {
        long targetTod = windowStart + offset;
        if (tod < windowStart) {
            return dayStart + targetTod;
        }
        if (tod >= windowEnd) {
            return dayStart + MaidWorldTime.DAY_TICKS + targetTod;
        }
        if (targetTod > tod) {
            return dayStart + targetTod;
        }
        return dayStart + MaidWorldTime.DAY_TICKS + targetTod;
    }
}
