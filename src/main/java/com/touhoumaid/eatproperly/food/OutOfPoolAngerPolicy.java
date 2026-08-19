package com.touhoumaid.eatproperly.food;

/**
 * Pure state machine for out-of-effective-pool player deliveries and maid anger.
 * Times use the same clock unit as {@code level.getGameTime()} (ticks).
 */
public final class OutOfPoolAngerPolicy {
    public static final int WINDOW_INITIAL_TICKS_DEFAULT = 2000;
    public static final int WINDOW_EXTEND_TICKS_DEFAULT = 1000;
    public static final int ANGER_THRESHOLD_DEFAULT = 10;
    public static final int ANGER_DURATION_TICKS_DEFAULT = 12000;
    public static final int GUI_BUBBLE_COOLDOWN_TICKS_DEFAULT = 80;

    private OutOfPoolAngerPolicy() {
    }

    /**
     * @param windowEnd exclusive end of the accumulation window ({@code now >= windowEnd} → expired)
     * @param count     number of out-of-pool <em>stow events</em> in the current window
     *                  (each batch/put counts as 1, not stack size)
     * @param angerEnd  exclusive end of anger ({@code now >= angerEnd} → not angry)
     */
    public record State(long windowEnd, int count, long angerEnd) {
        public static State idle() {
            return new State(0L, 0, 0L);
        }
    }

    public record Result(State state, boolean shouldStow, boolean becameAngry, boolean forgave) {
    }

    public static boolean isAngry(long now, long angerEnd) {
        return angerEnd > 0L && now < angerEnd;
    }

    public static boolean canShowGuiBlockBubble(long now, long lastBubbleAt, int cooldownTicks) {
        if (lastBubbleAt <= 0L) {
            return true;
        }
        return now - lastBubbleAt >= cooldownTicks;
    }

    /**
     * Successful out-of-pool stow while not angry.
     * {@code amount} only gates whether the event counts ({@code > 0}); the counter always
     * increases by <strong>1</strong> per call so a whole stack/batch is one strike.
     */
    public static Result onOutOfPoolStowed(
            long now,
            int amount,
            State before,
            int windowInitialTicks,
            int windowExtendTicks,
            int angerThreshold,
            int angerDurationTicks) {
        if (amount <= 0) {
            return new Result(before, true, false, false);
        }
        State cleared = expireWindowIfNeeded(now, before);
        int count;
        long windowEnd;
        if (cleared.windowEnd() <= 0L || now >= cleared.windowEnd()) {
            count = 1;
            windowEnd = now + windowInitialTicks;
        } else {
            count = cleared.count() + 1;
            windowEnd = cleared.windowEnd() + windowExtendTicks;
        }
        if (count >= angerThreshold) {
            State angry = new State(0L, 0, now + angerDurationTicks);
            return new Result(angry, true, true, false);
        }
        return new Result(new State(windowEnd, count, cleared.angerEnd()), true, false, false);
    }

    /** Out-of-pool attempt while already angry: do not stow; refresh anger duration. */
    public static Result onOutOfPoolWhileAngry(long now, State before, int angerDurationTicks) {
        return new Result(new State(0L, 0, now + angerDurationTicks), false, false, false);
    }

    /** In-pool delivery while angry: clear anger (caller stows and speaks forgive). */
    public static Result onForgive(long now, State before) {
        return new Result(State.idle(), true, false, true);
    }

    /** Drop expired accumulation window so the next stow starts fresh. */
    public static State expireWindowIfNeeded(long now, State state) {
        if (state.windowEnd() > 0L && now >= state.windowEnd()) {
            return new State(0L, 0, state.angerEnd());
        }
        return state;
    }

    /** Clear anger when the deadline has passed. */
    public static State expireAngerIfNeeded(long now, State state) {
        if (state.angerEnd() > 0L && now >= state.angerEnd()) {
            return new State(state.windowEnd(), state.count(), 0L);
        }
        return state;
    }
}
