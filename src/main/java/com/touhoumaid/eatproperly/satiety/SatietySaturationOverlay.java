package com.touhoumaid.eatproperly.satiety;

/**
 * How to blit the golden saturation overlay on one satiety cake icon.
 * Half overlays must clip to the left half so the base (non-gold) cake remains on the right.
 */
public final class SatietySaturationOverlay {
    public enum Kind {
        NONE,
        FULL,
        /** Left half of the full cake UV only — do not blit the HALF sprite (its right is dark). */
        HALF_LEFT
    }

    private SatietySaturationOverlay() {
    }

    /** Each icon covers two saturation points, same bands as hunger icons. */
    public static Kind kindForIcon(float saturation, int iconIndex) {
        if (saturation > iconIndex * 2 + 1) {
            return Kind.FULL;
        }
        if (saturation > iconIndex * 2) {
            return Kind.HALF_LEFT;
        }
        return Kind.NONE;
    }

    public static int destWidth(Kind kind, int iconSize) {
        return switch (kind) {
            case NONE -> 0;
            case FULL -> iconSize;
            case HALF_LEFT -> iconSize / 2;
        };
    }

    public static int srcWidth(Kind kind, int srcSize) {
        return switch (kind) {
            case NONE -> 0;
            case FULL -> srcSize;
            case HALF_LEFT -> srcSize / 2;
        };
    }

    /** Half overlays need a non-gold FULL cake right half under the gold left (HALF food base is dark). */
    public static boolean needsNormalCakeRightUnderlay(Kind kind) {
        return kind == Kind.HALF_LEFT;
    }

    public static int halfDestOffset(int iconSize) {
        return iconSize / 2;
    }

    public static int halfSrcUOffset(int srcSize) {
        return srcSize / 2;
    }

    /** Half overlays clip the full cake sprite; never use the half sprite (dark right half). */
    public static boolean usesFullCakeUv(Kind kind) {
        return kind == Kind.FULL || kind == Kind.HALF_LEFT;
    }
}
