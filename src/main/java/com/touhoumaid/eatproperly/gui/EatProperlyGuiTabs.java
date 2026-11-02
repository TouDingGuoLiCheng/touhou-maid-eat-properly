package com.touhoumaid.eatproperly.gui;

import com.touhoumaid.eatproperly.compat.TouhouMaidAffectionCompat;
import net.minecraftforge.fml.ModList;

/**
 * Dynamic maid GUI tab id for Eat Properly. Vanilla TLM uses 0-4; addon tabs must not collide.
 */
public final class EatProperlyGuiTabs {
    /** Fallback when GUI inspection is unavailable (server-side openMaidGui). */
    private static final int FALLBACK_TAB_ID = 5;

    /** Touhou Maid: Affection bond tab id ({@code MaidTabButton} third ctor arg). */
    private static final int AFFECTION_BOND_TAB_ID = 207;

    private static volatile int cachedTabId = -1;

    private EatProperlyGuiTabs() {
    }

    public static int tabId() {
        if (cachedTabId >= 0) {
            return cachedTabId;
        }
        return allocateFallbackTabId();
    }

    public static void cacheTabId(int tabId) {
        cachedTabId = tabId;
    }

    public static boolean isOurTab(int tabId) {
        return tabId == tabId();
    }

    /** Highest tab id we know other loaded addons may already occupy (server-safe). */
    public static int knownForeignTabIdCeiling() {
        int max = 4;
        if (ModList.get().isLoaded(TouhouMaidAffectionCompat.MOD_ID)) {
            max = Math.max(max, AFFECTION_BOND_TAB_ID);
        }
        return max;
    }

    private static int allocateFallbackTabId() {
        return Math.max(FALLBACK_TAB_ID, knownForeignTabIdCeiling() + 1);
    }
}
