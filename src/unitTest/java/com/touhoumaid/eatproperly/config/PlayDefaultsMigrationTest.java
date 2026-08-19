package com.touhoumaid.eatproperly.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayDefaultsMigrationTest {

    @Test
    void skipsWhenRevisionAlreadyCurrent() {
        PlayDefaultsMigration.Decision d = PlayDefaultsMigration.decide(
                PlayDefaultsMigration.REVISION_CURRENT,
                5,
                8);
        assertFalse(d.shouldBumpRevision());
        assertFalse(d.migrateAbandonDays());
        assertFalse(d.migrateBingeUntil());
    }

    @Test
    void migratesLegacyDefaultsAndBumpsRevision() {
        PlayDefaultsMigration.Decision d = PlayDefaultsMigration.decide(0, 5, 8);
        assertTrue(d.shouldBumpRevision());
        assertTrue(d.migrateAbandonDays());
        assertTrue(d.migrateBingeUntil());
        assertEquals(PlayDefaultsMigration.REQUEST_ABANDON_DAYS_DEFAULT, d.abandonDaysAfter());
        assertEquals(PlayDefaultsMigration.HUNGRY_BINGE_UNTIL_SATIETY_DEFAULT, d.bingeUntilAfter());
    }

    @Test
    void preservesCustomValuesButStillBumpsRevision() {
        PlayDefaultsMigration.Decision d = PlayDefaultsMigration.decide(0, 12, 6);
        assertTrue(d.shouldBumpRevision());
        assertFalse(d.migrateAbandonDays());
        assertFalse(d.migrateBingeUntil());
        assertEquals(12, d.abandonDaysAfter());
        assertEquals(6, d.bingeUntilAfter());
    }

    @Test
    void migratesOnlyTheKeyStillOnLegacyDefault() {
        PlayDefaultsMigration.Decision abandonOnly = PlayDefaultsMigration.decide(0, 5, 10);
        assertTrue(abandonOnly.migrateAbandonDays());
        assertFalse(abandonOnly.migrateBingeUntil());

        PlayDefaultsMigration.Decision bingeOnly = PlayDefaultsMigration.decide(0, 10, 8);
        assertFalse(bingeOnly.migrateAbandonDays());
        assertTrue(bingeOnly.migrateBingeUntil());
    }
}
