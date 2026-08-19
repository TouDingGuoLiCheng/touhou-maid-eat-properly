package com.touhoumaid.eatproperly.favorability;

/**
 * Flat favorability points granted when a food request is completed.
 */
public final class RequestFavorPolicy {
    /** Ordinary completion (lunchbox / backpack / beg hand-in, etc.). */
    public static final int NORMAL_POINTS = 192;
    /** Dual lunchbox when both slots qualify ({@code canCompleteDual}). */
    public static final int DUAL_POINTS = 384;

    private RequestFavorPolicy() {
    }

    /**
     * Points actually added: {@code grant} clamped so current + result never exceeds {@code maxPoint}.
     */
    public static int clampedGrant(int currentFavor, int grant, int maxPoint) {
        if (grant <= 0) {
            return 0;
        }
        int room = Math.max(0, maxPoint - currentFavor);
        return Math.min(grant, room);
    }
}
