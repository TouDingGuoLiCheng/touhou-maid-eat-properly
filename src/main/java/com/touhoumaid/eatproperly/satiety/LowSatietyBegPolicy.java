package com.touhoumaid.eatproperly.satiety;

/**
 * Pure rules for low-satiety begging (N1): when the maid may beg, and what right-click give does.
 */
public final class LowSatietyBegPolicy {
    /** Beg while satiety is strictly below this. */
    public static final int SATIETY_BELOW = 8;
    /** Max distance to owner (blocks, inclusive). */
    public static final double OWNER_RANGE = 6.0D;
    /** Match TLM {@code MaidBegTask} approach feel. */
    public static final float APPROACH_SPEED = 0.6F;
    public static final int CLOSE_ENOUGH = 2;

    public enum GiveAction {
        /** Not our path (non-begging or not pool food). */
        REJECT,
        /** Put 1 in backpack and complete the current request. */
        COMPLETE_REQUEST,
        /** Put 1 in backpack only. */
        STOW_ONLY
    }

    private LowSatietyBegPolicy() {
    }

    public static boolean satietyAllows(int satiety) {
        return satiety < SATIETY_BELOW;
    }

    public static boolean ownerInRange(double distanceToOwner) {
        return distanceToOwner <= OWNER_RANGE;
    }

    /**
     * @param beggingActive this mod's low-satiety beg flag
     * @param inEffectivePool held stack is in the maid's effective request pool
     * @param matchesCurrentRequest held stack matches an active food request
     */
    public static GiveAction decideGive(boolean beggingActive, boolean inEffectivePool, boolean matchesCurrentRequest) {
        if (!beggingActive || !inEffectivePool) {
            return GiveAction.REJECT;
        }
        return matchesCurrentRequest ? GiveAction.COMPLETE_REQUEST : GiveAction.STOW_ONLY;
    }
}
