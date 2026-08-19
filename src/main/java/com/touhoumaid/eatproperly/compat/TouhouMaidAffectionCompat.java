package com.touhoumaid.eatproperly.compat;

/**
 * Soft compatibility with Touhou Maid: Affection when Eat Properly extends favor past vanilla level 3.
 */
public final class TouhouMaidAffectionCompat {
    public static final String MOD_ID = "touhou_maid_affection";

    /** Vanilla TLM max favor level before Eat Properly extension. */
    public static final int VANILLA_MAX_FAVOR_LEVEL = 3;

    private TouhouMaidAffectionCompat() {
    }

    /**
     * Affection maps kiss cooldown and Maids Prayer amplifier with {@code switch (level)} cases 1–3 only;
     * any higher level hits {@code default} and incorrectly uses the level-0 tier.
     */
    public static int clampAffectionTier(int level) {
        return level > VANILLA_MAX_FAVOR_LEVEL ? VANILLA_MAX_FAVOR_LEVEL : level;
    }
}
