package com.touhoumaid.eatproperly.food;

/**
 * Pure rules for cooldowns that only advance while a maid is loaded/ticking,
 * and for spreading deferred public-chat delivery times after an unload gap.
 */
public final class LoadedTimePolicy {
    private LoadedTimePolicy() {
    }

    /** True when the maid missed enough world ticks to imply chunk unload (or long freeze). */
    public static boolean isUnloadGap(long lastSeenGameTime, long nowGameTime, long gapThresholdTicks) {
        if (lastSeenGameTime < 0L || gapThresholdTicks <= 0L) {
            return false;
        }
        return nowGameTime - lastSeenGameTime > gapThresholdTicks;
    }

    /**
     * Advances loaded-only elapsed cooldown ticks. On an unload gap, elapsed is unchanged
     * (cooldown paused for the missing time).
     */
    public static long advanceElapsed(long elapsedTicks, long deltaTicks, boolean unloadGap) {
        if (unloadGap || deltaTicks <= 0L) {
            return Math.max(0L, elapsedTicks);
        }
        return Math.max(0L, elapsedTicks) + deltaTicks;
    }

    /**
     * Offsets within an awake window so deferred chats do not all land on the same moment.
     * Values are in {@code [0, windowLength)}.
     */
    public static int[] staggeredOffsets(int count, int windowLength, int baseOffset, int staggerTicks) {
        if (count <= 0 || windowLength <= 0) {
            return new int[0];
        }
        int stagger = Math.max(1, staggerTicks);
        int[] offsets = new int[count];
        for (int i = 0; i < count; i++) {
            offsets[i] = Math.floorMod(baseOffset + i * (long) stagger, windowLength);
        }
        return offsets;
    }
}
