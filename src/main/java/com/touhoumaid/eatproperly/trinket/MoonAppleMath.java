package com.touhoumaid.eatproperly.trinket;

/**
 * Pure fractional-accrual math (no Minecraft types) for the moon apple candy: restores about
 * {@link #DAILY_RESTORE_AMOUNT} hunger points per in-game day ({@link #TICKS_PER_DAY} ticks),
 * spread evenly tick-by-tick. Since hunger is integer-only, leftover fractional progress must be
 * carried forward (persisted by the caller) instead of being dropped each tick.
 */
public final class MoonAppleMath {
    /** ~20 points restored per full game day. */
    public static final double DAILY_RESTORE_AMOUNT = 20.0;
    public static final long TICKS_PER_DAY = 24_000L;
    public static final double RATE_PER_TICK = DAILY_RESTORE_AMOUNT / TICKS_PER_DAY;

    private MoonAppleMath() {
    }

    /**
     * Advances fractional restore progress by {@code elapsedTicks} and extracts whole restore
     * points accumulated so far.
     *
     * @param storedProgress leftover fractional progress (0.0 inclusive to 1.0 exclusive normally,
     *                        but any non-negative value is accepted defensively)
     * @param elapsedTicks   ticks elapsed since the last accrual check; negative values are treated as 0
     * @return the whole points to restore now, plus the new leftover progress to persist
     */
    public static Result accrue(double storedProgress, long elapsedTicks) {
        double safeElapsed = Math.max(0L, elapsedTicks);
        double progress = Math.max(0.0, storedProgress) + safeElapsed * RATE_PER_TICK;
        int wholePoints = (int) Math.floor(progress);
        double leftover = progress - wholePoints;
        return new Result(wholePoints, leftover);
    }

    public record Result(int wholePoints, double leftoverProgress) {
    }
}
