package com.touhoumaid.eatproperly.snackraid;

/**
 * Approach feel for the two ways a maid walks up to a container: sneaking for a snack raid,
 * or openly fetching with a Food Permit.
 */
public final class SnackRaidApproachPolicy {
    /** Sneaky shuffle used when raiding without a permit. */
    public static final float STEALTH_SPEED = SnackRaidFleePolicy.APPROACH_SPEED;
    /** Permit fetch is brisker than sneaking, but the walk must still read as a walk. */
    public static final float DEFAULT_PERMIT_SPEED = 0.35F;
    public static final float MAX_PERMIT_SPEED = 0.6F;

    private SnackRaidApproachPolicy() {
    }

    public static float approachSpeed(boolean foodPermitMode, double configuredPermitSpeed) {
        if (!foodPermitMode) {
            return STEALTH_SPEED;
        }
        return (float) Math.min(MAX_PERMIT_SPEED, Math.max(STEALTH_SPEED, configuredPermitSpeed));
    }

    /** Permit fetches are done in the open, so the "don't look at me" lines do not apply. */
    public static boolean shouldSpeakStealthApproach(boolean foodPermitMode) {
        return !foodPermitMode;
    }
}
