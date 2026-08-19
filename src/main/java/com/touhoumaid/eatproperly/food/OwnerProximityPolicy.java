package com.touhoumaid.eatproperly.food;

/** Pure owner-distance checks for maid speech gates. */
public final class OwnerProximityPolicy {
    private OwnerProximityPolicy() {
    }

    /** {@code rangeBlocks} is a Euclidean radius; compares against distance squared. */
    public static boolean isWithinRange(double distanceSq, double rangeBlocks) {
        if (rangeBlocks <= 0.0D) {
            return false;
        }
        return distanceSq <= rangeBlocks * rangeBlocks;
    }
}
