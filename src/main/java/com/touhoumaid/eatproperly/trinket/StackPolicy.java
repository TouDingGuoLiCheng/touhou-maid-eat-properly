package com.touhoumaid.eatproperly.trinket;

/**
 * How multiple equipped copies of the same buff trinket (or, for {@link #MAX}, several different
 * trinkets granting the same kind of bonus) combine into one effective value. Pure (no Minecraft
 * types) so the merge math can be unit tested without booting Forge; see {@link TrinketEffects}
 * for the Minecraft-side wiring that counts equipped items and calls into this class.
 */
public enum StackPolicy {
    /** Value scales linearly with equipped count: N copies contribute N * perUnit (e.g. heart_seasoning). */
    ADDITIVE,
    /** Any equipped count &gt; 0 activates the effect exactly once; extra copies change nothing. */
    UNIQUE,
    /** Only the single strongest value applies; extra copies of an equal value change nothing. */
    MAX;

    /** Merges {@code perUnit} across {@code count} equipped copies of the same item, as an int. */
    public int mergeInt(int count, int perUnit) {
        int safeCount = Math.max(0, count);
        return switch (this) {
            case ADDITIVE -> safeCount * perUnit;
            case UNIQUE, MAX -> safeCount > 0 ? perUnit : 0;
        };
    }

    /** Merges {@code perUnit} across {@code count} equipped copies of the same item, as a double. */
    public double mergeDouble(int count, double perUnit) {
        int safeCount = Math.max(0, count);
        return switch (this) {
            case ADDITIVE -> safeCount * perUnit;
            case UNIQUE, MAX -> safeCount > 0 ? perUnit : 0.0;
        };
    }

    /** True when {@code count} equipped copies are enough to activate this policy at all. */
    public boolean isActive(int count) {
        return count > 0;
    }

    /** Largest of any number of raw values, e.g. combining {@link #MAX}-policy bonuses from distinct items. */
    public static double maxOf(double... values) {
        double result = 0.0;
        for (double value : values) {
            result = Math.max(result, value);
        }
        return result;
    }
}
