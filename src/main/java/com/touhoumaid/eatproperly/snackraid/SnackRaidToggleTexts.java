package com.touhoumaid.eatproperly.snackraid;

/** Pure constants/helpers shared by GUI and packet feedback. */
public final class SnackRaidToggleTexts {
    public static final String ALLOWED_KEY = "gui.touhou_maid_eat_properly.snack_raid_allowed";
    public static final String DISALLOWED_KEY = "gui.touhou_maid_eat_properly.snack_raid_disallowed";
    public static final String BUTTON_ALLOWED_KEY = "gui.touhou_maid_eat_properly.snack_raid_button_allowed";
    public static final String BUTTON_DISALLOWED_KEY = "gui.touhou_maid_eat_properly.snack_raid_button_disallowed";
    public static final String TOOLTIP_ALLOWED_KEY = "gui.touhou_maid_eat_properly.snack_raid_tooltip_allowed";
    public static final String TOOLTIP_DISALLOWED_KEY = "gui.touhou_maid_eat_properly.snack_raid_tooltip_disallowed";
    public static final String TOOLTIP_DISABLED_BY_CONFIG_KEY =
            "gui.touhou_maid_eat_properly.snack_raid_tooltip_disabled_by_config";

    private SnackRaidToggleTexts() {
    }

    public static boolean defaultAllowed() {
        return true;
    }

    public static String feedbackKey(boolean allowed) {
        return allowed ? ALLOWED_KEY : DISALLOWED_KEY;
    }

    public static String buttonKey(boolean allowed) {
        return allowed ? BUTTON_ALLOWED_KEY : BUTTON_DISALLOWED_KEY;
    }

    public static String tooltipKey(boolean allowed) {
        return allowed ? TOOLTIP_ALLOWED_KEY : TOOLTIP_DISALLOWED_KEY;
    }
}
