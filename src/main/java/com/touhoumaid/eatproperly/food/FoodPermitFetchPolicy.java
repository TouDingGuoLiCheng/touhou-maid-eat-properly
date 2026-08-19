package com.touhoumaid.eatproperly.food;

/**
 * Pure trigger/priority/cooldown rules for Food Permit fetches.
 */
public final class FoodPermitFetchPolicy {
    public static final int DEFAULT_BACKPACK_TAKE = 5;
    public static final int DEFAULT_PICNIC_TAKE = 16;
    public static final int DEFAULT_COOLDOWN_TICKS = 600;

    public enum Destination {
        NONE,
        BACKPACK,
        PICNIC
    }

    public enum StartAction {
        NONE,
        FOOD_PERMIT,
        SNACK_RAID
    }

    public record FetchPlan(Destination destination, int maxTake) {
        public static final FetchPlan NONE = new FetchPlan(Destination.NONE, 0);
    }

    private FoodPermitFetchPolicy() {
    }

    /**
     * @param resting whether the maid is currently in rest/home-meal context
     * @param picnicHasFood whether the picnic basket already has effective-pool food
     * @param handOrBackpackHasFood whether hands/backpack already have effective-pool food
     */
    public static FetchPlan decide(boolean resting, boolean picnicHasFood, boolean handOrBackpackHasFood) {
        return decide(resting, picnicHasFood, handOrBackpackHasFood, false);
    }

    /**
     * @param picnicRefillSeeking left an empty home-meal mat to fetch refill; keep picnic ≤16 even if
     *                            she is no longer counted as "resting" after {@code stopRiding}
     */
    public static FetchPlan decide(boolean resting, boolean picnicHasFood, boolean handOrBackpackHasFood,
                                   boolean picnicRefillSeeking) {
        if (picnicRefillSeeking && !handOrBackpackHasFood) {
            return new FetchPlan(Destination.PICNIC, DEFAULT_PICNIC_TAKE);
        }
        if (resting && !picnicHasFood) {
            return new FetchPlan(Destination.PICNIC, DEFAULT_PICNIC_TAKE);
        }
        if (!handOrBackpackHasFood) {
            return new FetchPlan(Destination.BACKPACK, DEFAULT_BACKPACK_TAKE);
        }
        return FetchPlan.NONE;
    }

    public static StartAction decideStart(boolean hasMarkedReachableTarget, boolean snackRaidFallbackAvailable) {
        if (hasMarkedReachableTarget) {
            return StartAction.FOOD_PERMIT;
        }
        if (snackRaidFallbackAvailable) {
            return StartAction.SNACK_RAID;
        }
        return StartAction.NONE;
    }

    public static boolean isOffCooldown(long now, long lastFetchTime, int cooldownTicks) {
        return cooldownTicks <= 0 || now - lastFetchTime >= cooldownTicks;
    }
}
