package com.touhoumaid.eatproperly.food;

/**
 * Pure rules for placing picnic-meal leftovers into nearby world containers.
 */
public final class PicnicLeftoverPolicy {
    /** Inclusive Chebyshev radius from the picnic mat center block. */
    public static final int SEARCH_RADIUS = 6;
    /** When sit association misses the TE, scan this Chebyshev radius around the maid for a mat. */
    public static final int MAT_LOOKUP_RADIUS = 8;

    private PicnicLeftoverPolicy() {
    }

    public static boolean shouldAttemptPlacement(boolean picnicMatKnown) {
        return picnicMatKnown;
    }

    public static boolean shouldAttemptPlacement(boolean picnicMatKnown, boolean leftoverEmpty) {
        return picnicMatKnown && !leftoverEmpty;
    }

    /** Prefer the tile's recorded center when present (multi-block picnic mat). */
    public static boolean shouldUseTileCenter(boolean tileCenterKnown) {
        return tileCenterKnown;
    }

    /** Inclusive Chebyshev distance on each axis vs {@code radius}. */
    public static boolean isWithinRadius(int dx, int dy, int dz, int radius) {
        return Math.abs(dx) <= radius && Math.abs(dy) <= radius && Math.abs(dz) <= radius;
    }
}
