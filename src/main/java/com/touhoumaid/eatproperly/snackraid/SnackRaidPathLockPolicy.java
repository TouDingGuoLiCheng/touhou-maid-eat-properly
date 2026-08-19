package com.touhoumaid.eatproperly.snackraid;

/** Pure decisions used by the runtime snack-raid path lock. */
public final class SnackRaidPathLockPolicy {
    private static final int MIN_LOCK_RADIUS = 64;
    private static final double FOLLOW_TELEPORT_MARGIN = 2.0D;

    private SnackRaidPathLockPolicy() {
    }

    public static int lockRadius(int originalRadius) {
        return Math.max(originalRadius, MIN_LOCK_RADIUS);
    }

    public static boolean ownerTooFar(double ownerDistance, int originalRadius) {
        return ownerDistance > originalRadius + FOLLOW_TELEPORT_MARGIN;
    }
}
