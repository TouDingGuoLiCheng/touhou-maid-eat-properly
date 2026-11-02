package com.touhoumaid.eatproperly.client;

import com.github.tartaricacid.touhoulittlemaid.api.event.client.MaidContainerGuiEvent;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.MaidTabButton;
import com.touhoumaid.eatproperly.gui.EatProperlyGuiTabs;
import com.touhoumaid.eatproperly.gui.MaidTopTabPlacement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Places addon maid GUI tabs after built-in tabs and any tabs other mods already registered.
 * Layout matches Touhou Maid: Affection {@code BondTabLayout}.
 */
public final class MaidGuiTabLayout {
    public static final int TOP_TAB_Y_OFFSET = 5;

    private static final Field EVENT_BUTTONS_FIELD = findEventButtonsField();
    private static final Field MAID_TAB_ID_FIELD = findMaidTabIdField();

    private MaidGuiTabLayout() {
    }

    public static int resolveTopTabX(MaidContainerGuiEvent.Init event, AbstractMaidContainerGui<?> gui) {
        List<Integer> topTabXs = new ArrayList<>();
        int leftPos = event.getLeftPos();
        int topPos = event.getTopPos();
        for (var child : gui.children()) {
            if (child instanceof MaidTabButton tab && tab.getY() == topPos + TOP_TAB_Y_OFFSET) {
                topTabXs.add(tab.getX());
            }
        }
        collectEventButtonTopTabs(event, topPos, topTabXs);
        return MaidTopTabPlacement.nextTopTabX(leftPos, topTabXs.stream().mapToInt(Integer::intValue).toArray());
    }

    /** Next unused custom tab id after vanilla tabs (0-4) and known addon tabs on the GUI row. */
    public static int resolveNextTabId(MaidContainerGuiEvent.Init event, AbstractMaidContainerGui<?> gui) {
        int maxId = 4;
        for (var child : gui.children()) {
            if (child instanceof MaidTabButton tab) {
                maxId = Math.max(maxId, readTabId(tab));
            }
        }
        maxId = Math.max(maxId, collectEventButtonTabIds(event));
        maxId = Math.max(maxId, EatProperlyGuiTabs.knownForeignTabIdCeiling());
        return maxId + 1;
    }

    static int nextTopTabX(int leftPos, int[] existingTopTabXs) {
        return MaidTopTabPlacement.nextTopTabX(leftPos, existingTopTabXs);
    }

    @SuppressWarnings("unchecked")
    private static void collectEventButtonTopTabs(MaidContainerGuiEvent.Init event, int topPos, List<Integer> topTabXs) {
        if (EVENT_BUTTONS_FIELD == null) {
            return;
        }
        try {
            Object value = EVENT_BUTTONS_FIELD.get(event);
            if (!(value instanceof Map<?, ?> buttons)) {
                return;
            }
            for (Object button : buttons.values()) {
                if (button instanceof MaidTabButton tab && tab.getY() == topPos + TOP_TAB_Y_OFFSET) {
                    topTabXs.add(tab.getX());
                }
            }
        } catch (IllegalAccessException ignored) {
            // TLM does not expose the event button collection; GUI children still cover the built-in tabs.
        }
    }

    @SuppressWarnings("unchecked")
    private static int collectEventButtonTabIds(MaidContainerGuiEvent.Init event) {
        if (EVENT_BUTTONS_FIELD == null) {
            return 4;
        }
        int maxId = 4;
        try {
            Object value = EVENT_BUTTONS_FIELD.get(event);
            if (!(value instanceof Map<?, ?> buttons)) {
                return maxId;
            }
            for (Object button : buttons.values()) {
                if (button instanceof MaidTabButton tab) {
                    maxId = Math.max(maxId, readTabId(tab));
                }
            }
        } catch (IllegalAccessException ignored) {
        }
        return maxId;
    }

    private static int readTabId(MaidTabButton tab) {
        if (MAID_TAB_ID_FIELD == null) {
            return 4;
        }
        try {
            return MAID_TAB_ID_FIELD.getInt(tab);
        } catch (IllegalAccessException ignored) {
            return 4;
        }
    }

    private static Field findEventButtonsField() {
        try {
            // Field lives on MaidContainerGuiEvent, not Init (matches Affection BondMaidGuiTabHandler).
            Field field = MaidContainerGuiEvent.class.getDeclaredField("buttons");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | RuntimeException ignored) {
            return null;
        }
    }

    private static Field findMaidTabIdField() {
        try {
            Field field = MaidTabButton.class.getDeclaredField("left");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | RuntimeException ignored) {
            return null;
        }
    }
}
