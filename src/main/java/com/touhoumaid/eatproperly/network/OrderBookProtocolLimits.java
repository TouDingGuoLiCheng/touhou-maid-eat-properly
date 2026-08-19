package com.touhoumaid.eatproperly.network;

/** Bounds for Order Book sync packets. */
public final class OrderBookProtocolLimits {
    /** Large enough for full-select of all-mod edible pools (observed ~700+). */
    public static final int MAX_LISTED_IDS = 2048;
    public static final int MAX_ID_STRING_LENGTH = 256;

    private OrderBookProtocolLimits() {
    }

    public static boolean isValidListedIdCount(int count) {
        return count >= 0 && count <= MAX_LISTED_IDS;
    }

    public static java.util.List<String> boundedListedIds(java.util.Set<String> ids) {
        if (ids.size() <= MAX_LISTED_IDS) {
            return java.util.List.copyOf(ids);
        }
        return ids.stream().limit(MAX_LISTED_IDS).toList();
    }
}
