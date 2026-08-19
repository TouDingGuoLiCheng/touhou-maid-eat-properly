package com.touhoumaid.eatproperly.config;

/**
 * One-shot bump of shipped play defaults that Forge left at older values in existing toml files.
 * Custom player values are preserved; only the previous shipped defaults are rewritten.
 */
public final class PlayDefaultsMigration {
    public static final int REVISION_CURRENT = 1;

    public static final int REQUEST_ABANDON_DAYS_LEGACY_DEFAULT = 5;
    public static final int REQUEST_ABANDON_DAYS_DEFAULT = 10;

    public static final int HUNGRY_BINGE_UNTIL_SATIETY_LEGACY_DEFAULT = 8;
    public static final int HUNGRY_BINGE_UNTIL_SATIETY_DEFAULT = 10;

    private PlayDefaultsMigration() {
    }

    public static Decision decide(int revisionOnDisk, int abandonDays, int bingeUntil) {
        if (revisionOnDisk >= REVISION_CURRENT) {
            return new Decision(false, abandonDays, bingeUntil, false, false);
        }
        boolean migrateAbandon = abandonDays == REQUEST_ABANDON_DAYS_LEGACY_DEFAULT;
        boolean migrateBinge = bingeUntil == HUNGRY_BINGE_UNTIL_SATIETY_LEGACY_DEFAULT;
        return new Decision(
                true,
                migrateAbandon ? REQUEST_ABANDON_DAYS_DEFAULT : abandonDays,
                migrateBinge ? HUNGRY_BINGE_UNTIL_SATIETY_DEFAULT : bingeUntil,
                migrateAbandon,
                migrateBinge);
    }

    public record Decision(
            boolean shouldBumpRevision,
            int abandonDaysAfter,
            int bingeUntilAfter,
            boolean migrateAbandonDays,
            boolean migrateBingeUntil) {
    }
}
