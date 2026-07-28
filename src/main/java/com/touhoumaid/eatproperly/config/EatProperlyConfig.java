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
    /** Speech after a hypnosis note successfully forgets the current request. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HYPNOSIS_SPEECH;

    public static final ForgeConfigSpec.DoubleValue SATIETY_DAILY_DRAIN_RATIO;
    public static final ForgeConfigSpec.IntValue SATIETY_LOW_THRESHOLD;
    /** Maid work/home meals only when satiety is strictly below this value. */
    public static final ForgeConfigSpec.IntValue EAT_WHEN_SATIETY_BELOW;
    public static final ForgeConfigSpec.IntValue HUNGER_SPEECH_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue FAVOR_DECAY_GRACE_DAYS;
    public static final ForgeConfigSpec.IntValue FAVOR_DECAY_PER_DAY_EXP;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HUNGER_SPEECH;

    /**
     * Reserved legacy milestone override table. Empty by default; parsed but not applied to gameplay.
     * The real milestone reward levels/pools are hardcoded in {@code RewardPoolTables}; this list is
     * kept only for possible forward-compatible per-level overrides.
     * Future entries may look like {@code "10"} (favor level) or {@code "level=10;reward=none"}.
     */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MILESTONES;
    /** Chat+bubble when a new pending gift is locked (once per item). Use {maid}. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MILESTONE_GIFT_SPEECH;
    /** Bubble-only nag while gifts remain unclaimed. Use {maid}. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MILESTONE_GIFT_BUBBLE;
    /** When the reward pool is empty for a milestone level. Use {maid}. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MILESTONE_EMPTY_SPEECH;

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
        HYPNOSIS_SPEECH = builder
                .comment("Speech after a hypnosis note makes the maid forget her current food request.")
                .defineList("hypnosisSpeech", List.of(
                        "诶？我想吃什么来着……",
                        "脑袋晕乎乎的……今天想吃啥忘记了……",
                        "唔……点单？好像想不起来了……"
                ), o -> o instanceof String);
        builder.pop();

        builder.push("satiety");
        SATIETY_DAILY_DRAIN_RATIO = builder
                .comment("Fraction of max satiety (20) drained each game day at day change (getGameTime/24000). Default 0.30.")
                .defineInRange("satietyDailyDrainRatio", 0.30D, 0.0D, 1.0D);
        SATIETY_LOW_THRESHOLD = builder
                .comment("Below this satiety, hunger speech and favor-decay grace counting start. Default 6.")
                .defineInRange("satietyLowThreshold", 6, 0, 20);
        EAT_WHEN_SATIETY_BELOW = builder
                .comment(
                        "Work meal and home meal only attempt eating when satiety < this value.",
                        "Heal-self meal (eating when hurt) is not gated. Default 15."
                )
                .defineInRange("eatWhenSatietyBelow", 15, 0, 21);
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
                        "Optional legacy milestone override table strings. Default empty; not required for rewards to work.",
                        "Milestone reward levels and pools are already active and hardcoded (see RewardPoolTables);",
                        "this list is reserved for possible future per-level overrides."
                )
                .defineListAllowEmpty("milestones", ArrayList::new, o -> o instanceof String);
        MILESTONE_GIFT_SPEECH = builder
                .comment("Said once per new pending gift (chat + bubble). Use {maid} for display name.")
                .defineList("milestoneGiftSpeech", List.of(
                        "我有东西给你哦，主人~",
                        "诶嘿……给主人准备了小礼物，记得来拿呀～",
                        "主人主人！我藏了一样东西给你哦。",
                        "尾巴都摇起来了……因为有礼物要送给主人！",
                        "唔，有点害羞……但我真的想把这个交给主人。",
                        "叮咚——心意快递到啦，请主人收下。",
                        "今天也要好好吃饭，还有……请收下我的一点小心意吧。",
                        "我认真想了很久才选的！快来领走它～"
                ), o -> o instanceof String);
        MILESTONE_GIFT_BUBBLE = builder
                .comment("Repeating bubble while unclaimed gifts remain. Use {maid}. Keep short.")
                .defineList("milestoneGiftBubble", List.of(
                        "我有东西给你哦，主人~",
                        "主人，过来一下嘛～"
                ), o -> o instanceof String);
        MILESTONE_EMPTY_SPEECH = builder
                .comment("When a milestone fires but the pool has nothing left to give. Use {maid}.")
                .defineList("milestoneEmptySpeech", List.of(
                        "想送你礼物来着……可是好像没有了呢。",
                        "欸？口袋空空的……下次一定准备好！",
                        "礼物清单翻到底了……只剩谢谢你了，主人。",
                        "呜，想送礼却拿不出新东西……抱一下可以吗？"
                ), o -> o instanceof String);
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
