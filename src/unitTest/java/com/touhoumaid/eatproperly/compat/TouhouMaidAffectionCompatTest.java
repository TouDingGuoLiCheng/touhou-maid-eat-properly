package com.touhoumaid.eatproperly.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TouhouMaidAffectionCompatTest {
    @Test
    void affectionTierStaysWithinVanillaSwitchCases() {
        assertEquals(0, TouhouMaidAffectionCompat.clampAffectionTier(0));
        assertEquals(1, TouhouMaidAffectionCompat.clampAffectionTier(1));
        assertEquals(2, TouhouMaidAffectionCompat.clampAffectionTier(2));
        assertEquals(3, TouhouMaidAffectionCompat.clampAffectionTier(3));
        assertEquals(3, TouhouMaidAffectionCompat.clampAffectionTier(4));
        assertEquals(3, TouhouMaidAffectionCompat.clampAffectionTier(100));
    }
}
