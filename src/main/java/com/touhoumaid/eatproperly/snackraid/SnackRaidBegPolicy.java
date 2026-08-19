package com.touhoumaid.eatproperly.snackraid;

/**
 * Whether TLM's own beg task may run. A maid mid-raid is busy with the chest, so the owner's
 * snack must not pull her into the begging pose (and away from our walk target).
 */
public final class SnackRaidBegPolicy {
    private SnackRaidBegPolicy() {
    }

    public static boolean allowBeg(boolean raidActive, boolean attractionAllowed) {
        return attractionAllowed && !raidActive;
    }
}
