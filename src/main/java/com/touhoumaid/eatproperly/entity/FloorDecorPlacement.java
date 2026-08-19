package com.touhoumaid.eatproperly.entity;

/**
 * Floor figurine placement helpers (pure JVM).
 */
public final class FloorDecorPlacement {
    private FloorDecorPlacement() {
    }

    /** Model front faces the placing player. */
    public static float yawFacingPlayer(float playerYRot) {
        return playerYRot + 180.0F;
    }

    /**
     * Feet on top center of the supporting block (block coords → entity feet position).
     */
    public static double[] feetOnBlockTop(int blockX, int blockY, int blockZ) {
        return new double[]{blockX + 0.5D, blockY + 1.0D, blockZ + 0.5D};
    }
}
