package com.touhoumaid.eatproperly.client;

/**
 * Client-side survival-inventory trinket panel fold state.
 */
public final class TrinketPanelClient {
    private static boolean open;

    private TrinketPanelClient() {
    }

    public static boolean isOpen() {
        return open;
    }

    public static void setOpen(boolean value) {
        open = value;
    }

    public static void toggle() {
        open = !open;
    }
}
