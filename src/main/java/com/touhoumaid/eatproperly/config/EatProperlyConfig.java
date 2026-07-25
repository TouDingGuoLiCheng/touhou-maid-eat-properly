package com.touhoumaid.eatproperly.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Common config for Touhou Maid: Eat properly.
 * <p>
 * <b>Reload policy:</b> Forge COMMON config. Values are read via {@code .get()} at use time,
 * so changing {@code config/touhou_maid_eat_properly-common.toml} takes effect after Forge
 * reloads the config (re-enter the world / restart the server is the reliable way).
 * No separate in-game hot-reload command is required.
 * <p>
 * <b>Game day:</b> {@code level.getGameTime() / 24000} (Minecraft day index from world time).
 */
public final class EatProperlyConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_FAVOR_LEVEL;
    public static final ForgeConfigSpec.IntValue EXP_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue HP_BONUS_PER_LEVEL_AFTER_3;

    public static final ForgeConfigSpec.IntValue REQUEST_INTERVAL_DAYS_MIN;
    public static final ForgeConfigSpec.IntValue REQUEST_INTERVAL_DAYS_MAX;
    public static final ForgeConfigSpec.IntValue REQUEST_NAG_INTERVAL_TICKS_MIN;
    public static final ForgeConfigSpec.IntValue REQUEST_NAG_INTERVAL_TICKS_MAX;
    public static final ForgeConfigSpec.IntValue REQUEST_ABANDON_DAYS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> REQUEST_SPEECH;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> THANKS_SPEECH;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ABANDON_SPEECH;

    public static final ForgeConfigSpec.DoubleValue SATIETY_DAILY_DRAIN_RATIO;
    public static final ForgeConfigSpec.IntValue SATIETY_LOW_THRESHOLD;
    public static final ForgeConfigSpec.IntValue HUNGER_SPEECH_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue FAVOR_DECAY_GRACE_DAYS;
    public static final ForgeConfigSpec.IntValue FAVOR_DECAY_PER_DAY_EXP;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HUNGER_SPEECH;

    /**
     * Reserved milestone table. Empty by default; parsed but unused in v1.
     * Future entries may look like {@code "10"} (favor level) or {@code "level=10;reward=none"}.
     */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MILESTONES;

    /** Same as player food level. */
    public static final int SATIETY_MAX = 20;

    /** Vanilla TLM thresholds kept for levels 0-3. */
    public static final int VANILLA_LEVEL_3_POINT = 384;
    public static final int VANILLA_LEVEL_3_HEALTH = 80;

    /** Custom maid GUI tab id (TLM uses 0-4). */
    public static final int GUI_TAB_ID = 5;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment(
                "Touhou Maid: Eat properly — common config",
                "Reload: edit this file, then re-enter the world / restart the server (Forge COMMON).",
                "Game day = getGameTime() / 24000."
        );

        builder.push("favorability");
        MAX_FAVOR_LEVEL = builder
                .comment("Maximum favorability level (vanilla max is 3). Default 100.")
                .defineInRange("maxFavorLevel", 100, 3, 1000);
        EXP_PER_LEVEL = builder
                .comment("Favorability points required for each level after level 3. Default 192.")
                .defineInRange("expPerLevel", 192, 1, 100000);
        HP_BONUS_PER_LEVEL_AFTER_3 = builder
                .comment("Max health added per level after level 3. Default 2 (one heart).")
                .defineInRange("hpBonusPerLevelAfter3", 2, 0, 100);
        builder.pop();

        builder.push("foodRequest");
        REQUEST_INTERVAL_DAYS_MIN = builder
                .comment("Minimum game days between food requests. Default 3.")
                .defineInRange("requestIntervalDaysMin", 3, 1, 365);
        REQUEST_INTERVAL_DAYS_MAX = builder
                .comment("Maximum game days between food requests. Default 10.")
                .defineInRange("requestIntervalDaysMax", 10, 1, 365);
        REQUEST_NAG_INTERVAL_TICKS_MIN = builder
                .comment("Minimum ticks between request chat reminders. Default 15000.")
                .defineInRange("requestNagIntervalTicksMin", 15000, 200, 720000);
        REQUEST_NAG_INTERVAL_TICKS_MAX = builder
                .comment("Maximum ticks between request chat reminders. Default 28000.",
                        "Each reminder rolls a new random delay in [min, max].")
                .defineInRange("requestNagIntervalTicksMax", 28000, 200, 720000);
        REQUEST_ABANDON_DAYS = builder
                .comment("Game days without fulfillment before the maid gives up the request. Default 5.")
                .defineInRange("requestAbandonDays", 5, 1, 365);
        REQUEST_SPEECH = builder
                .comment("Speech templates when requesting food. Use {food} for food name.")
                .defineList("requestSpeech", List.of(
                        "主人，我想吃{food}，好不好嘛",
                        "诶嘿……可以给我{food}吗？",
                        "主人～想吃{food}！",
                        "拜托拜托，想要{food}",
                        "今天想吃{food}呢"
                ), o -> o instanceof String);
        THANKS_SPEECH = builder
                .comment("Speech templates when request is completed. Use {food} for food name.")
                .defineList("thanksSpeech", List.of(
                        "哇，主人真的给我做了{food}，谢谢你主人！",
                        "收到{food}啦，好开心！",
                        "谢谢主人的{food}～",
                        "嗯嗯，{food}很好吃的样子！",
                        "有{food}真幸福，最喜欢主人了"
                ), o -> o instanceof String);
        ABANDON_SPEECH = builder
                .comment("Speech when giving up an unfulfilled request. Use {food} for food name.")
                .defineList("abandonSpeech", List.of(
                        "主人很忙对吗？我明白的，提出这样的请求很抱歉……",
                        "没事的，主人不用在意……只是有点想吃{food}而已。",
                        "看来这次是我太任性了……对不起，主人。",
                        "呜呜……没关系的，主人一定有更重要的事要做吧。",
                        "我还是自己忍一下好了……抱歉打扰你了，主人。"
                ), o -> o instanceof String);
        builder.pop();

        builder.push("satiety");
        SATIETY_DAILY_DRAIN_RATIO = builder
                .comment("Fraction of max satiety (20) drained each game day at day change (getGameTime/24000). Default 0.30.")
                .defineInRange("satietyDailyDrainRatio", 0.30D, 0.0D, 1.0D);
        SATIETY_LOW_THRESHOLD = builder
                .comment("Below this satiety, hunger speech and favor-decay grace counting start. Default 6.")
                .defineInRange("satietyLowThreshold", 6, 0, 20);
        HUNGER_SPEECH_INTERVAL_TICKS = builder
                .comment("Ticks between hunger chat/bubble while satiety is low. Default 24000 (1 game day).")
                .defineInRange("hungerSpeechIntervalTicks", 24000, 200, 720000);
        FAVOR_DECAY_GRACE_DAYS = builder
                .comment("Game days of continuous low satiety before favor starts decaying. Default 5.")
                .defineInRange("favorDecayGraceDays", 5, 0, 365);
        FAVOR_DECAY_PER_DAY_EXP = builder
                .comment("Favor points removed per day after grace while still low. Default -1 = half of expPerLevel.")
                .defineInRange("favorDecayPerDayExp", -1, -1, 100000);
        HUNGER_SPEECH = builder
                .comment("Speech templates when satiety is low. Use {maid} for maid display name.")
                .defineList("hungerSpeech", List.of(
                        "{maid}有点饿了……",
                        "肚子咕咕叫了，主人……",
                        "好饿呀，想吃点东西",
                        "主人，{maid}快要饿扁了",
                        "再不吃饭，{maid}要没有力气了"
                ), o -> o instanceof String);
        builder.pop();

        builder.push("milestones");
        MILESTONES = builder
                .comment(
                        "Reserved milestone table for future rewards. Default empty = no effect in v1.",
                        "Entries are parsed for forward compatibility but currently ignored.",
                        "Suggested future format: a favor level number, e.g. \"10\", or \"level=10;reward=none\"."
                )
                .defineListAllowEmpty("milestones", ArrayList::new, o -> o instanceof String);
        builder.pop();

        SPEC = builder.build();
    }

    /** Effective daily favor decay; -1 in config means half a level of exp. */
    public static int favorDecayPerDayExp() {
        int configured = FAVOR_DECAY_PER_DAY_EXP.get();
        if (configured < 0) {
            return Math.max(1, EXP_PER_LEVEL.get() / 2);
        }
        return configured;
    }

    /** Raw milestone strings from config (may be empty). */
    public static List<String> milestoneEntries() {
        List<? extends String> list = MILESTONES.get();
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(list);
    }

    private EatProperlyConfig() {
    }
}
