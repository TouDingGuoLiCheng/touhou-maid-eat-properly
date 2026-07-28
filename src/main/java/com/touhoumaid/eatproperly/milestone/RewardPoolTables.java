package com.touhoumaid.eatproperly.milestone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntUnaryOperator;

/**
 * Pure milestone pool tables (no Minecraft types) for unit testing.
 */
public final class RewardPoolTables {
    private static final Map<Integer, RewardCategory> LEVEL_CATEGORY;
    private static final EnumMap<RewardCategory, List<String>> POOLS;
    public static final String MOD_ID = "touhou_maid_eat_properly";

    static {
        Map<Integer, RewardCategory> levels = new LinkedHashMap<>();
        levels.put(5, RewardCategory.TOKEN_LOW);
        levels.put(10, RewardCategory.CARE);
        levels.put(20, RewardCategory.BUFF);
        levels.put(30, RewardCategory.TOKEN_LOW);
        levels.put(40, RewardCategory.CARE);
        levels.put(50, RewardCategory.BUFF);
        levels.put(60, RewardCategory.TOKEN_MID);
        levels.put(70, RewardCategory.CARE);
        levels.put(80, RewardCategory.BUFF);
        levels.put(90, RewardCategory.TOKEN_HIGH);
        levels.put(100, RewardCategory.TOKEN_HIGH);
        LEVEL_CATEGORY = Collections.unmodifiableMap(levels);

        EnumMap<RewardCategory, List<String>> pools = new EnumMap<>(RewardCategory.class);
        pools.put(RewardCategory.TOKEN_LOW, List.of(
                "forget_me_not_bouquet",
                "praise_certificate",
                "thanks_note_empty_box",
                "paper_crane",
                "meal_bell",
                "thanks_candy",
                "half_strawberry_cake",
                "field_photo",
                "banner_honest_boss"
        ));
        pools.put(RewardCategory.TOKEN_MID, List.of(
                "heart_gem",
                "handwritten_letter",
                "moonlight_shard",
                "two_tone_hair_tie",
                "embroidered_handkerchief",
                "paired_cup",
                "unopened_souvenir",
                "pressed_diary_page",
                "block_figurine",
                "silver_bell_charm"
        ));
        pools.put(RewardCategory.TOKEN_HIGH, List.of(
                "warm_amulet",
                "eternal_meal_ticket",
                "heart_knot",
                "sugar_jar",
                "unnamed_ring",
                "plain_pouch"
        ));
        pools.put(RewardCategory.CARE, List.of(
                "bound_lunchbox",
                "dual_lunchbox",
                "extra_meal_permit",
                "hypnosis_note"
        ));
        pools.put(RewardCategory.BUFF, List.of(
                "heart_seasoning",
                "insulated_meal_bag",
                "moon_apple_candy",
                "scarlet_amulet",
                "night_sparrow_wrapper",
                "sweet_soft_candy",
                "onsen_manju_bag",
                "sobering_raisin",
                "lake_fairy_keepsake"
        ));
        POOLS = pools;
    }

    private RewardPoolTables() {
    }

    public static Optional<RewardCategory> categoryForLevel(int level) {
        return Optional.ofNullable(LEVEL_CATEGORY.get(level));
    }

    public static boolean isMilestoneLevel(int level) {
        return LEVEL_CATEGORY.containsKey(level);
    }

    public static List<String> poolPaths(RewardCategory category) {
        return POOLS.getOrDefault(category, List.of());
    }

    public static Optional<String> pickPath(RewardCategory category, Set<String> excludePaths, IntUnaryOperator nextInt) {
        List<String> pool = poolPaths(category);
        if (pool.isEmpty()) {
            return Optional.empty();
        }
        List<String> candidates = new ArrayList<>();
        for (String path : pool) {
            if (!isExcluded(path, excludePaths)) {
                candidates.add(path);
            }
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(nextInt.applyAsInt(candidates.size())));
    }

    private static boolean isExcluded(String path, Set<String> excludePaths) {
        if (excludePaths.contains(path)) {
            return true;
        }
        return excludePaths.contains(MOD_ID + ":" + path);
    }
}
