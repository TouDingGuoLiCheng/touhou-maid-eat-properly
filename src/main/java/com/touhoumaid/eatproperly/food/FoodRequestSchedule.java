package com.touhoumaid.eatproperly.food;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Pure scheduling helpers for food-request day gaps: per-maid random intervals and
 * same-owner day deconfliction (prefer [{@code min},{@code max}], then walk forward).
 */
public final class FoodRequestSchedule {
    private static final int AVOID_LAST_ATTEMPTS = 8;

    private FoodRequestSchedule() {
    }

    /**
     * Picks an interval in [{@code min},{@code max}], preferring a value other than
     * {@code lastInterval} when the range has more than one choice.
     */
    public static int rollInterval(Random random, int min, int max, int lastInterval) {
        int lo = Math.min(min, max);
        int hi = Math.max(min, max);
        if (lo < 1) {
            lo = 1;
        }
        if (hi < lo) {
            hi = lo;
        }
        if (lo == hi) {
            return lo;
        }
        for (int i = 0; i < AVOID_LAST_ATTEMPTS; i++) {
            int rolled = lo + random.nextInt(hi - lo + 1);
            if (rolled != lastInterval) {
                return rolled;
            }
        }
        return lo + random.nextInt(hi - lo + 1);
    }

    /**
     * Chooses the absolute next-request game day.
     * <p>
     * Prefers {@code today + rolledInterval} if free; otherwise a random free day in
     * [{@code today+min},{@code today+max}]; if that window is full, the first free day
     * at or after {@code today+max+1}.
     *
     * @param occupiedDays absolute game-day indices already claimed by other same-owner maids
     * @return {@code long[2]} of {@code {chosenDay, intervalUsed}} where intervalUsed is
     *         {@code chosenDay - today}
     */
    public static long[] pickNextRequestDay(long today, int min, int max, int lastInterval,
                                            Set<Long> occupiedDays, Random random) {
        int lo = Math.max(1, Math.min(min, max));
        int hi = Math.max(lo, Math.max(min, max));
        Set<Long> occupied = occupiedDays != null ? occupiedDays : Collections.emptySet();

        int preferredInterval = rollInterval(random, lo, hi, lastInterval);
        long preferredDay = today + preferredInterval;
        if (!occupied.contains(preferredDay)) {
            return new long[]{preferredDay, preferredInterval};
        }

        List<Long> freeInWindow = new ArrayList<>(hi - lo + 1);
        for (int d = lo; d <= hi; d++) {
            long candidate = today + d;
            if (!occupied.contains(candidate)) {
                freeInWindow.add(candidate);
            }
        }
        if (!freeInWindow.isEmpty()) {
            long chosen = freeInWindow.get(random.nextInt(freeInWindow.size()));
            return new long[]{chosen, chosen - today};
        }

        long day = today + hi + 1L;
        while (occupied.contains(day)) {
            day++;
        }
        return new long[]{day, day - today};
    }
}
