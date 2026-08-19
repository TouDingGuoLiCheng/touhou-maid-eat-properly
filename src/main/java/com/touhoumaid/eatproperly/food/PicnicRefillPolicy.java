package com.touhoumaid.eatproperly.food;

/**
 * Pure rules for the resting home-meal picnic-mat refill (N3.10).
 *
 * <p>While a maid rests on a home-meal picnic mat whose food ran out, she should not stay glued
 * to the mat nagging. Prefer topping the mat up from what she already carries; only if she has no
 * pool food anywhere should she leave to fetch from a marked container.
 */
public final class PicnicRefillPolicy {
    /** How long (ticks) the leave-to-fetch intent stays armed before we give up and let her re-seat. */
    public static final int SEEK_TIMEOUT_TICKS = 1200;

    public enum Action {
        /** Nothing to do (not resting, or the mat already has food). */
        NONE,
        /** Move carried pool food into the mat in place; no need to get up. */
        FLUSH_IN_PLACE,
        /** No pool food carried; leave the mat to fetch from a marked container. */
        LEAVE_TO_FETCH
    }

    private PicnicRefillPolicy() {
    }

    /**
     * @param restingOnHomeMeal maid is seated on an ON_HOME_MEAL picnic mat
     * @param matHasPoolFood     the seated mat already holds effective-pool food
     * @param carriesPoolFood    hands/backpack hold effective-pool food
     * @param markedTargetReachable a marked container with pool food is within range
     */
    public static Action decide(boolean restingOnHomeMeal, boolean matHasPoolFood,
                                boolean carriesPoolFood, boolean markedTargetReachable) {
        if (!restingOnHomeMeal || matHasPoolFood) {
            return Action.NONE;
        }
        if (carriesPoolFood) {
            return Action.FLUSH_IN_PLACE;
        }
        if (markedTargetReachable) {
            return Action.LEAVE_TO_FETCH;
        }
        return Action.NONE;
    }

    /**
     * Clear leave-to-fetch when seated back on the original mat, the home mat is gone, or timed out.
     * Carrying food alone must not clear — she still has to return to that mat.
     */
    public static boolean shouldClearSeek(boolean seatedOnOriginalMat, boolean homeMatMissing,
                                          long now, long seekSince, int timeoutTicks) {
        if (seatedOnOriginalMat || homeMatMissing) {
            return true;
        }
        return timeoutTicks > 0 && now - seekSince >= timeoutTicks;
    }

    /** While seeking without carried food, block FindHomeMeal so she is not glued back to the empty mat. */
    public static boolean shouldSuppressFindHomeMeal(boolean seeking, boolean carriesPoolFood) {
        return seeking && !carriesPoolFood;
    }

    /** After fetch, steer FindHomeMeal to the remembered original mat instead of the nearest POI. */
    public static boolean shouldForceOriginalMat(boolean seeking, boolean carriesPoolFood, boolean hasOriginalMat) {
        return seeking && carriesPoolFood && hasOriginalMat;
    }

    /**
     * While the maid is mid-use (home-meal eating animation), do not flush hands back onto an empty
     * mat. Otherwise the last extracted food is immediately returned → ghost eat.
     */
    public static boolean shouldDeferWhileUsingItem(boolean usingItem) {
        return usingItem;
    }

    /**
     * Empty-mat refill must start Food Permit even when satiety is still above the normal eat line.
     */
    public static boolean shouldBypassFoodPermitSatiety(boolean seekingPicnicRefill) {
        return seekingPicnicRefill;
    }

    /**
     * Mid-fetch must not {@code finish} for high satiety while empty-mat seeking — otherwise
     * {@code beginRaid} starts and the next tick aborts into aimless wandering.
     *
     * @param satietyWouldAbort result of the normal satiety abort check
     */
    public static boolean shouldAbortPermitFetchForSatiety(boolean seekingPicnicRefill, boolean satietyWouldAbort) {
        if (shouldBypassFoodPermitSatiety(seekingPicnicRefill)) {
            return false;
        }
        return satietyWouldAbort;
    }

    /** Empty-mat seeking must not wait out the shared Food Permit success cooldown. */
    public static boolean shouldBypassFoodPermitCooldown(boolean seekingPicnicRefill) {
        return seekingPicnicRefill;
    }

    /**
     * @param seekingPicnicRefill {@link PicnicRefillData} leave-to-fetch intent is armed
     * @param satiety             current maid satiety
     * @param fetchThreshold      normal Food Permit satiety gate ({@code eatWhen} / low threshold max)
     */
    public static boolean allowsFoodPermitStart(boolean seekingPicnicRefill, int satiety, int fetchThreshold) {
        if (shouldBypassFoodPermitSatiety(seekingPicnicRefill)) {
            return true;
        }
        return satiety < fetchThreshold;
    }
}
