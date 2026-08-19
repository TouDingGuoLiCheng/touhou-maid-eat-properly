package com.touhoumaid.eatproperly.entity;

/**
 * Pure placement math for wall-hung rewards (no Minecraft types — unit-test friendly).
 * Face steps are the clicked face normal (air is at clicked + face).
 */
public final class WallHangPlacement {
    private WallHangPlacement() {
    }

    /**
     * Entity hang block (air cell used as {@code HangingEntity} pos).
     * Banner (2 high): pos is the lower of the two cells so vanilla survives() checks
     * support behind pos and pos.above() — i.e. clicked.below() and clicked when player
     * aimed at the upper wall block.
     */
    public static int[] hangPos(int clickedX, int clickedY, int clickedZ,
                                int faceX, int faceY, int faceZ,
                                boolean twoBlocksTall) {
        int airX = clickedX + faceX;
        int airY = clickedY + faceY;
        int airZ = clickedZ + faceZ;
        if (twoBlocksTall) {
            return new int[]{airX, airY - 1, airZ};
        }
        return new int[]{airX, airY, airZ};
    }

    /**
     * Banner needs the clicked block (upper) and the block below it as solid supports.
     */
    public static boolean canHangTwoHigh(boolean clickedSupportSolid, boolean belowClickedSolid) {
        return clickedSupportSolid && belowClickedSolid;
    }

    /** Pixel height passed to {@code HangingEntity}: avoid multiples of 32 for 1×1 so offs=0. */
    public static int hangPixelHeight(boolean twoBlocksTall) {
        return twoBlocksTall ? 32 : 12;
    }

    public static int hangPixelWidth() {
        return 16;
    }
}
