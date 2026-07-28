package com.touhoumaid.eatproperly.favorability;

import org.junit.jupiter.api.Test;

import java.util.function.IntUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FavorLevelMathTest {
    /** Toy level table: 0/64/192/384/576/... (step 192 after level 2), mirrors the shape of the real one closely enough for math tests. */
    private static final int[] THRESHOLDS = {0, 64, 192, 384, 576, 768};

    private static int pointOf(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level >= THRESHOLDS.length) {
            return THRESHOLDS[THRESHOLDS.length - 1] + (level - (THRESHOLDS.length - 1)) * 192;
        }
        return THRESHOLDS[level];
    }

    /** Mirrors {@code FavorabilityExtension.getLevel}, which clamps at a max level while
     *  {@code getPointByLevel} keeps extrapolating linearly past it. */
    private static final int MAX_LEVEL = 5;

    private static int levelOf(int favor) {
        int level = 0;
        while (level + 1 <= MAX_LEVEL && pointOf(level + 1) <= favor) {
            level++;
        }
        return level;
    }

    private static final IntUnaryOperator LEVEL_OF = FavorLevelMathTest::levelOf;
    private static final IntUnaryOperator POINT_OF = FavorLevelMathTest::pointOf;

    @Test
    void singleLevelGainMatchesNextLevelPoint() {
        // favor=100 -> level 1 (>=64), next level (2) needs 192 total -> add 92
        assertEquals(92, FavorLevelMath.pointsToAdd(100, 1, MAX_LEVEL, LEVEL_OF, POINT_OF));
    }

    @Test
    void doubleLevelGainSkipsTwoThresholds() {
        // favor=100 -> level 1, +2 levels -> level 3 threshold = 384 -> add 284
        assertEquals(284, FavorLevelMath.pointsToAdd(100, 2, MAX_LEVEL, LEVEL_OF, POINT_OF));
    }

    @Test
    void zeroOrNegativeGainAddsNothing() {
        assertEquals(0, FavorLevelMath.pointsToAdd(100, 0, MAX_LEVEL, LEVEL_OF, POINT_OF));
        assertEquals(0, FavorLevelMath.pointsToAdd(100, -1, MAX_LEVEL, LEVEL_OF, POINT_OF));
    }

    @Test
    void neverReturnsNegativeWhenAlreadyPastTarget() {
        // Level clamps at MAX_LEVEL while the point formula keeps extrapolating, so a maxed-out
        // maid's favor can exceed the "next" threshold. Must not return a negative add amount.
        assertEquals(0, FavorLevelMath.pointsToAdd(10_000, 1, MAX_LEVEL, LEVEL_OF, POINT_OF));
    }

    @Test
    void exactBoundaryFavorStillComputesForward() {
        // favor sits exactly on a level threshold.
        assertEquals(192, FavorLevelMath.pointsToAdd(192, 1, MAX_LEVEL, LEVEL_OF, POINT_OF));
    }

    @Test
    void atExactlyMaxLevelThresholdFurtherGainAddsNothing() {
        // Favor sitting exactly on the max-level threshold: a "+1" must not add phantom favor
        // past the cap (this is the bug being fixed — target level must clamp to maxLevel).
        int favorAtMax = pointOf(MAX_LEVEL);
        assertEquals(0, FavorLevelMath.pointsToAdd(favorAtMax, 1, MAX_LEVEL, LEVEL_OF, POINT_OF));
    }

    @Test
    void dualLevelGainNearMaxClampsToMaxLevelThresholdInsteadOfOvershooting() {
        // One level below max; a naive +2 would target level (MAX_LEVEL + 1), which the unclamped
        // point formula would still happily compute. Must clamp to the MAX_LEVEL threshold instead.
        int favorOneBelowMax = pointOf(MAX_LEVEL - 1);
        int expected = pointOf(MAX_LEVEL) - favorOneBelowMax;
        assertEquals(expected, FavorLevelMath.pointsToAdd(favorOneBelowMax, 2, MAX_LEVEL, LEVEL_OF, POINT_OF));
        // Sanity: without the clamp this would instead equal pointOf(MAX_LEVEL + 1) - favorOneBelowMax,
        // which is strictly larger since pointOf keeps growing past MAX_LEVEL.
        assertTrue(expected < pointOf(MAX_LEVEL + 1) - favorOneBelowMax);
    }
}
