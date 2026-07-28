package com.touhoumaid.eatproperly.favorability;

import java.util.function.IntUnaryOperator;

/**
 * Pure favorability-point math (no Minecraft types) so multi-level grants (e.g. dual lunchbox +2)
 * can be unit tested without a live {@code FavorabilityManager}.
 */
public final class FavorLevelMath {
    private FavorLevelMath() {
    }

    /**
     * Points that must be added to {@code currentFavor} to land exactly on the threshold for
     * {@code levelGain} levels above the current one, clamped so the target level never exceeds
     * {@code maxLevel}. Never negative.
     *
     * <p>The clamp matters because {@code pointOf} (i.e. {@code FavorabilityExtension.getPointByLevel})
     * keeps extrapolating linearly past the max level while {@code levelOf} clamps — without it, a
     * maid already at (or near) the max level could still be granted "phantom" favor past the cap.
     */
    public static int pointsToAdd(int currentFavor, int levelGain, int maxLevel,
                                   IntUnaryOperator levelOf, IntUnaryOperator pointOf) {
        if (levelGain <= 0) {
            return 0;
        }
        int currentLevel = levelOf.applyAsInt(currentFavor);
        int targetLevel = Math.min(currentLevel + levelGain, maxLevel);
        int targetPoints = pointOf.applyAsInt(targetLevel);
        return Math.max(0, targetPoints - currentFavor);
    }
}
