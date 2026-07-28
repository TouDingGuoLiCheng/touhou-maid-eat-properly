package com.touhoumaid.eatproperly.milestone;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Milestone pools with Minecraft {@link ResourceLocation} helpers.
 */
public final class RewardPools {
    private RewardPools() {
    }

    public static Optional<RewardCategory> categoryForLevel(int level) {
        return RewardPoolTables.categoryForLevel(level);
    }

    public static boolean isMilestoneLevel(int level) {
        return RewardPoolTables.isMilestoneLevel(level);
    }

    public static List<String> poolPaths(RewardCategory category) {
        return RewardPoolTables.poolPaths(category);
    }

    public static Optional<String> pickPath(RewardCategory category, Set<String> excludePaths, RandomSource random) {
        return RewardPoolTables.pickPath(category, excludePaths, random::nextInt);
    }

    public static Optional<String> pickPath(RewardCategory category, Set<String> excludePaths,
                                            java.util.function.IntUnaryOperator nextInt) {
        return RewardPoolTables.pickPath(category, excludePaths, nextInt);
    }

    public static Optional<ResourceLocation> pick(RewardCategory category, Set<ResourceLocation> exclude, RandomSource random) {
        Set<String> excludePaths = exclude.stream()
                .map(RewardPools::toComparablePath)
                .collect(Collectors.toSet());
        return pickPath(category, excludePaths, random).map(RewardPools::id);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, path);
    }

    private static String toComparablePath(ResourceLocation loc) {
        if (EatProperlyMod.MOD_ID.equals(loc.getNamespace())) {
            return loc.getPath();
        }
        return loc.toString();
    }
}
