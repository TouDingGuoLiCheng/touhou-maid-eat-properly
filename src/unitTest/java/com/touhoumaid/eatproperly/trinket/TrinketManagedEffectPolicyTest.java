package com.touhoumaid.eatproperly.trinket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrinketManagedEffectPolicyTest {
    @Test
    void unequippedWithoutPriorProvideDoesNotClear() {
        TrinketManagedEffectPolicy.Tick tick = TrinketManagedEffectPolicy.decide(false, false);
        assertFalse(tick.provide());
        assertFalse(tick.clear());
        assertFalse(tick.providingAfter());
    }

    @Test
    void unequipEdgeClearsOnceThenStops() {
        TrinketManagedEffectPolicy.Tick edge = TrinketManagedEffectPolicy.decide(false, true);
        assertFalse(edge.provide());
        assertTrue(edge.clear());
        assertFalse(edge.providingAfter());

        TrinketManagedEffectPolicy.Tick idle = TrinketManagedEffectPolicy.decide(false, edge.providingAfter());
        assertFalse(idle.provide());
        assertFalse(idle.clear());
        assertFalse(idle.providingAfter());
    }

    @Test
    void equippedProvidesWithoutClearing() {
        TrinketManagedEffectPolicy.Tick tick = TrinketManagedEffectPolicy.decide(true, false);
        assertTrue(tick.provide());
        assertFalse(tick.clear());
        assertTrue(tick.providingAfter());
    }

    @Test
    void staysEquippedKeepsProviding() {
        TrinketManagedEffectPolicy.Tick tick = TrinketManagedEffectPolicy.decide(true, true);
        assertTrue(tick.provide());
        assertFalse(tick.clear());
        assertTrue(tick.providingAfter());
    }

    @Test
    void decideIsDeterministicForFourStates() {
        assertEquals(TrinketManagedEffectPolicy.decide(false, false),
                TrinketManagedEffectPolicy.decide(false, false));
        assertEquals(TrinketManagedEffectPolicy.decide(false, true),
                TrinketManagedEffectPolicy.decide(false, true));
        assertEquals(TrinketManagedEffectPolicy.decide(true, false),
                TrinketManagedEffectPolicy.decide(true, false));
        assertEquals(TrinketManagedEffectPolicy.decide(true, true),
                TrinketManagedEffectPolicy.decide(true, true));
    }
}
