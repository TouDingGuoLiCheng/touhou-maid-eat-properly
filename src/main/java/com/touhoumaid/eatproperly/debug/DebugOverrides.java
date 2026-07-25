package com.touhoumaid.eatproperly.debug;

import java.util.OptionalInt;

/**
 * Session-wide debug overrides (not persisted). Cleared on server restart.
 */
public final class DebugOverrides {
    private static Integer requestDaysMin;
    private static Integer requestDaysMax;
    private static Integer nagTicksMin;
    private static Integer nagTicksMax;

    private DebugOverrides() {
    }

    public static OptionalInt requestDaysMin() {
        return requestDaysMin == null ? OptionalInt.empty() : OptionalInt.of(requestDaysMin);
    }

    public static OptionalInt requestDaysMax() {
        return requestDaysMax == null ? OptionalInt.empty() : OptionalInt.of(requestDaysMax);
    }

    public static OptionalInt nagTicksMin() {
        return nagTicksMin == null ? OptionalInt.empty() : OptionalInt.of(nagTicksMin);
    }

    public static OptionalInt nagTicksMax() {
        return nagTicksMax == null ? OptionalInt.empty() : OptionalInt.of(nagTicksMax);
    }

    public static void setRequestIntervalDays(int min, int max) {
        if (max < min) {
            int tmp = min;
            min = max;
            max = tmp;
        }
        requestDaysMin = Math.max(0, min);
        requestDaysMax = Math.max(0, max);
    }

    public static void clearRequestIntervalDays() {
        requestDaysMin = null;
        requestDaysMax = null;
    }

    public static void setNagIntervalTicks(int min, int max) {
        if (max < min) {
            int tmp = min;
            min = max;
            max = tmp;
        }
        nagTicksMin = Math.max(1, min);
        nagTicksMax = Math.max(1, max);
    }

    public static void clearNagIntervalTicks() {
        nagTicksMin = null;
        nagTicksMax = null;
    }

    public static void clearAll() {
        clearRequestIntervalDays();
        clearNagIntervalTicks();
    }

    public static boolean hasAny() {
        return requestDaysMin != null || nagTicksMin != null;
    }
}
