package com.touhoumaid.eatproperly.milestone;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.logging.LogUtils;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

/**
 * Reserved milestone API for a future version.
 * <p>
 * v1 only parses config entries so an empty (or any) {@code milestones} list is safe and has no gameplay effect.
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

    /**
     * Called when favor level changes. Intentionally empty in v1.
     */
    public static void onFavorLevelChange(EntityMaid maid, int oldLevel, int newLevel) {
        // Reserved: apply milestone rewards when crossing configured levels.
        if (LOGGER.isDebugEnabled() && !parseConfigured().isEmpty()) {
            LOGGER.debug("Milestone hook (no-op): maid={} {} -> {}, entries={}",
                    maid.getUUID(), oldLevel, newLevel, EatProperlyConfig.milestoneEntries().size());
        }
    }

    /** Parsed milestone row; rewards are placeholders only. */
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
                    // keep empty; still store raw for forward compatibility
                }
            }
            return new MilestoneEntry(level, raw, reward);
        }

        public boolean hasEffect() {
            return false; // v1: never applies rewards
        }
    }
}
