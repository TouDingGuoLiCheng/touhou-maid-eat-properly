package com.touhoumaid.eatproperly.snackraid;

/** Pure search-bound helpers (no Minecraft types) for unit tests and container scans. */
public final class SnackRaidSearchBounds {
    private SnackRaidSearchBounds() {
    }

    public static boolean contains(int maidX, int maidY, int maidZ, int posX, int posY, int posZ,
                                   int radius, int verticalRange) {
        int dx = Math.abs(posX - maidX);
        int dy = Math.abs(posY - maidY);
        int dz = Math.abs(posZ - maidZ);
        return dx <= radius && dy <= verticalRange && dz <= radius;
    }
}
