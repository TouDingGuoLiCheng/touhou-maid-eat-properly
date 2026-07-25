package com.touhoumaid.eatproperly.favorability;

import com.touhoumaid.eatproperly.config.EatProperlyConfig;

/**
 * Extends Touhou Little Maid favorability beyond vanilla level 3.
 * Levels 0-3 keep vanilla point thresholds and base attributes;
 * levels 4+ use a flat point cost and +HP only.
 */
public final class FavorabilityExtension {
    private FavorabilityExtension() {
    }

    public static int getMaxLevel() {
        return EatProperlyConfig.MAX_FAVOR_LEVEL.get();
    }

    public static int getExpPerLevel() {
        return EatProperlyConfig.EXP_PER_LEVEL.get();
    }

    public static int getHpBonusPerLevelAfter3() {
        return EatProperlyConfig.HP_BONUS_PER_LEVEL_AFTER_3.get();
    }

    public static int getMaxPoint() {
        return getPointByLevel(getMaxLevel());
    }

    public static int getLevel(int favorability) {
        if (favorability < 64) {
            return 0;
        }
        if (favorability < 192) {
            return 1;
        }
        if (favorability < EatProperlyConfig.VANILLA_LEVEL_3_POINT) {
            return 2;
        }
        int level = 3 + (favorability - EatProperlyConfig.VANILLA_LEVEL_3_POINT) / getExpPerLevel();
        return Math.min(level, getMaxLevel());
    }

    /**
     * Minimum favorability points required to be at the given level.
     */
    public static int getPointByLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level == 1) {
            return 64;
        }
        if (level == 2) {
            return 192;
        }
        if (level == 3) {
            return EatProperlyConfig.VANILLA_LEVEL_3_POINT;
        }
        return EatProperlyConfig.VANILLA_LEVEL_3_POINT + (level - 3) * getExpPerLevel();
    }

    public static int getHealthByLevel(int level) {
        if (level <= 0) {
            return 20;
        }
        if (level == 1) {
            return 30;
        }
        if (level == 2) {
            return 40;
        }
        if (level == 3) {
            return EatProperlyConfig.VANILLA_LEVEL_3_HEALTH;
        }
        return EatProperlyConfig.VANILLA_LEVEL_3_HEALTH + (level - 3) * getHpBonusPerLevelAfter3();
    }

    /** Attack stays at vanilla level-3 value for all levels &gt;= 3. */
    public static int getAttackByLevel(int level) {
        if (level <= 0) {
            return 2;
        }
        if (level == 1) {
            return 3;
        }
        if (level == 2) {
            return 4;
        }
        return 6;
    }

    public static double getLevelPercent(int favorability) {
        int level = getLevel(favorability);
        if (level >= getMaxLevel()) {
            return 0.0D;
        }
        if (level == 0) {
            return favorability / 64.0D;
        }
        int current = getPointByLevel(level);
        int next = getPointByLevel(level + 1);
        return (double) (favorability - current) / (double) (next - current);
    }

    public static int nextLevelPoint(int favorability) {
        int level = getLevel(favorability);
        if (level >= getMaxLevel()) {
            return 0;
        }
        return getPointByLevel(level + 1) - favorability;
    }
}
