package com.touhoumaid.eatproperly.satiety;

/**
 * Pure rule for hunger speech: if the maid already has edible backpack food,
 * prefer eating over complaining. If eat fails, fall through to speak so the
 * path cannot stall silently (A12-speech).
 */
public final class HungerSpeechEatFirstPolicy {
    public enum Decision {
        EAT_FIRST,
        SPEAK
    }

    private HungerSpeechEatFirstPolicy() {
    }

    public static Decision decide(boolean hasBackpackFood) {
        return hasBackpackFood ? Decision.EAT_FIRST : Decision.SPEAK;
    }

    /** Successful eat clears the hunger speech timer. */
    public static boolean shouldResetHungerElapsedAfterEat(boolean eatSucceeded) {
        return eatSucceeded;
    }

    /** Failed eat should not block hunger speech forever. */
    public static boolean shouldSpeakAfterFailedEat(boolean eatSucceeded) {
        return !eatSucceeded;
    }
}
