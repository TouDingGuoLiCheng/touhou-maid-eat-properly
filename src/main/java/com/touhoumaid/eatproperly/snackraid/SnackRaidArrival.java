package com.touhoumaid.eatproperly.snackraid;

/**
 * Distance decisions for the snack raid, free of Minecraft types so they can be unit tested.
 *
 * <p>Arrival is judged by distance only. An earlier version also required the maid to have zero
 * velocity and an idle navigator, which the maid's own brain keeps busy, so that phase never ended.
 */
public final class SnackRaidArrival {
    /** Vanilla pathfinding stops within ~0.45 blocks of the last node, and the brain drifts a bit further. */
    public static final double ARRIVE_DISTANCE = 1.5D;
    public static final double FLEE_ARRIVE_DISTANCE = 2.0D;
    /** The wait is cancelled once the maid ends up this far from the container, e.g. after being carried off. */
    public static final double WAIT_LEASH_DISTANCE = 4.0D;

    private SnackRaidArrival() {
    }

    public static boolean arrivedAtStand(double distanceSq) {
        return distanceSq <= square(ARRIVE_DISTANCE);
    }

    public static boolean arrivedAtFlee(double distanceSq) {
        return distanceSq <= square(FLEE_ARRIVE_DISTANCE);
    }

    public static boolean leftContainerDuringWait(double distanceSq) {
        return distanceSq > square(WAIT_LEASH_DISTANCE);
    }

    private static double square(double value) {
        return value * value;
    }
}
