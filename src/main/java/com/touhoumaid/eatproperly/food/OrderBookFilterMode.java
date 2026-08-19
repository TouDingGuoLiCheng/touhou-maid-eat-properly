package com.touhoumaid.eatproperly.food;

/** Black/white list mode for the Order Book bauble. */
public enum OrderBookFilterMode {
    WHITELIST,
    BLACKLIST;

    public static OrderBookFilterMode fromName(String name, OrderBookFilterMode fallback) {
        if (name == null || name.isEmpty()) {
            return fallback;
        }
        for (OrderBookFilterMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return fallback;
    }
}
