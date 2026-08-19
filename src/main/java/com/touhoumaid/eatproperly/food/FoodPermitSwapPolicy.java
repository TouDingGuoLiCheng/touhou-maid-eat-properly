package com.touhoumaid.eatproperly.food;

public final class FoodPermitSwapPolicy {
    public enum DumpTier {
        NON_POOL_FOOD,
        OTHER_NON_TOOL,
        TOOL
    }

    private FoodPermitSwapPolicy() {
    }

    public static DumpTier classifyDump(boolean isFood, boolean inEffectivePool, boolean isTool) {
        if (isTool) {
            return DumpTier.TOOL;
        }
        if (isFood && !inEffectivePool) {
            return DumpTier.NON_POOL_FOOD;
        }
        return DumpTier.OTHER_NON_TOOL;
    }

    /** Negative = a better than b. */
    public static int compareDumpPriority(DumpTier a, DumpTier b) {
        return Integer.compare(a.ordinal(), b.ordinal());
    }

    public static int decideTakeCount(int stackCount, int maxTake, boolean needWholeStack) {
        if (stackCount <= 0) {
            return 0;
        }
        if (needWholeStack) {
            return stackCount;
        }
        return Math.min(stackCount, Math.max(1, maxTake));
    }

    public static boolean needWholeStackToFitDump(boolean dumpFitsWithoutClearingFoodSlot) {
        return !dumpFitsWithoutClearingFoodSlot;
    }
}
