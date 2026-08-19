package com.touhoumaid.eatproperly.milestone;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.logging.LogUtils;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Milestone rewards: on favor level-up, lock a pending claim from the matching pool.
 */
public final class MilestoneRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private MilestoneRegistry() {
    }

    public static List<MilestoneEntry> parseConfigured() {
        List<String> raw = EatProperlyConfig.milestoneEntries();
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<MilestoneEntry> parsed = new ArrayList<>(raw.size());
        for (String line : raw) {
            if (line == null || line.isBlank()) {
                continue;
            }
            parsed.add(MilestoneEntry.parse(line.trim()));
        }
        return Collections.unmodifiableList(parsed);
    }

    public static void onFavorLevelChange(EntityMaid maid, int oldLevel, int newLevel) {
        if (newLevel <= oldLevel) {
            return;
        }
        for (int level = oldLevel + 1; level <= newLevel; level++) {
            Optional<RewardCategory> category = RewardPools.categoryForLevel(level);
            if (category.isEmpty()) {
                continue;
            }
            if (MilestoneData.hasClaimedLevel(maid, level) || MilestoneData.hasPendingLevel(maid, level)) {
                continue;
            }
            Optional<ResourceLocation> item = RewardPools.pick(
                    category.get(),
                    MilestoneData.getExcludedItems(maid),
                    maid.getRandom());
            if (item.isEmpty()) {
                MilestoneData.markClaimedEmpty(maid, level);
                String empty = MaidSpeech.formatMilestoneEmpty(maid, maid.getRandom());
                if (!empty.isEmpty()) {
                    MaidSpeech.bubbleToOwnerIfNear(maid, empty);
                }
                LOGGER.debug("Milestone pool empty: maid={} level={}", maid.getUUID(), level);
                continue;
            }
            MilestoneData.addPending(maid, level, item.get());
            if (!MilestoneRewards.wasChatAnnounced(maid, item.get())) {
                String gift = MaidSpeech.formatMilestoneGift(maid, maid.getRandom());
                if (!gift.isEmpty()) {
                    MaidSpeech.bubbleToOwnerIfNear(maid, gift);
                }
                MilestoneRewards.markChatAnnounced(maid, item.get());
                MilestoneRewards.setLastBubbleGameTime(maid, maid.level().getGameTime());
            }
            LOGGER.debug("Milestone pending: maid={} level={} item={}", maid.getUUID(), level, item.get());
        }
    }

    /** Parsed config row (legacy / optional overrides). */
    public record MilestoneEntry(OptionalInt level, String raw, String rewardId) {
        public static MilestoneEntry parse(String raw) {
            String reward = "none";
            OptionalInt level = OptionalInt.empty();

            if (raw.contains("=") || raw.contains(";")) {
                String[] parts = raw.split(";");
                for (String part : parts) {
                    String[] kv = part.split("=", 2);
                    if (kv.length != 2) {
                        continue;
                    }
                    String key = kv[0].trim().toLowerCase(Locale.ROOT);
                    String value = kv[1].trim();
                    if ("level".equals(key)) {
                        try {
                            level = OptionalInt.of(Integer.parseInt(value));
                        } catch (NumberFormatException ignored) {
                            // keep empty
                        }
                    } else if ("reward".equals(key)) {
                        reward = value;
                    }
                }
            } else {
                try {
                    level = OptionalInt.of(Integer.parseInt(raw));
                } catch (NumberFormatException ignored) {
                    // keep empty
                }
            }
            return new MilestoneEntry(level, raw, reward);
        }

        public boolean hasEffect() {
            return level.isPresent() && RewardPools.isMilestoneLevel(level.getAsInt());
        }
    }
}
