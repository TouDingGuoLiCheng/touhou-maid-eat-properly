package com.touhoumaid.eatproperly.food;

/**
 * When Food Permit open-fetch should use its own bubble lines (not snack-raid stealth speech).
 */
public final class FoodPermitSpeechPolicy {
    private FoodPermitSpeechPolicy() {
    }

    /** Once at trip start while walking to a marked container. */
    public static boolean speakPermitApproach(boolean foodPermitMode) {
        return foodPermitMode;
    }

    /** Once after a successful extract ({@code itemsMoved > 0}). */
    public static boolean speakPermitSuccess(boolean foodPermitMode, int itemsMoved) {
        return foodPermitMode && itemsMoved > 0;
    }

    /**
     * Permit trip lines are ambient on the maid (watch the walk), not owner-directed chat.
     * Skip the ≤16 owner-near gate so bubbles still show when the player is watching from farther.
     */
    public static boolean requireOwnerNearbyForBubble() {
        return false;
    }
}
