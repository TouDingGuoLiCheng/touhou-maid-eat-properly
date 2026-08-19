package com.touhoumaid.eatproperly.snackraid;

import java.util.Comparator;

/**
 * Pure flee helpers (no Minecraft types) for unit tests and snack-raid pathing.
 */
public final class SnackRaidFleePolicy {
    /** Same as historical approach speed used by {@link SnackRaidPathing}. */
    public static final float APPROACH_SPEED = 0.2F;
    /** Brisk walk away — faster than approach, not a pathfinding sprint. */
    public static final float FLEE_SPEED = 0.9F;
    /** Extra time so ~8-block flee at moderate speed can finish. */
    public static final int FLEE_TIMEOUT_TICKS = 120;

    private SnackRaidFleePolicy() {
    }

    /**
     * Unit vector from chest center toward maid. Falls back to +X when overlapping.
     *
     * @return {x, y, z} direction; y is always 0 for horizontal flee
     */
    public static double[] awayDirection(double maidX, double maidY, double maidZ,
                                         double chestCenterX, double chestCenterY, double chestCenterZ) {
        double dx = maidX - chestCenterX;
        double dz = maidZ - chestCenterZ;
        double lenSqr = dx * dx + dz * dz;
        if (lenSqr < 1.0E-4) {
            return new double[]{1.0, 0.0, 0.0};
        }
        double len = Math.sqrt(lenSqr);
        return new double[]{dx / len, 0.0, dz / len};
    }

    public static double distSqr(int x, int y, int z, int chestX, int chestY, int chestZ) {
        double dx = x - chestX;
        double dy = y - chestY;
        double dz = z - chestZ;
        return dx * dx + dy * dy + dz * dz;
    }

    /** Sort {@code int[]{x,y,z}} so farther-from-chest comes first. */
    public static Comparator<int[]> fartherFromChestFirst(int chestX, int chestY, int chestZ) {
        return (a, b) -> Double.compare(
                distSqr(b[0], b[1], b[2], chestX, chestY, chestZ),
                distSqr(a[0], a[1], a[2], chestX, chestY, chestZ));
    }

    public static boolean shouldAbortForSatiety(SnackRaidPhase phase, int satiety, int threshold) {
        if (phase == SnackRaidPhase.FLEE) {
            return false;
        }
        return satiety >= threshold;
    }

    /** Repath only when navigation finished, and at most once per interval (avoids spam stutter). */
    public static boolean shouldRepathWhileFleeing(boolean navigationDone, int ticks, int interval) {
        if (!navigationDone) {
            return false;
        }
        return ticks == 1 || ticks % interval == 0;
    }
}
