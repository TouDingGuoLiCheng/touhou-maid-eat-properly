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
 * <b>Satiety game day:</b> TLM schedule day from {@code level.getDayTime()} (increments at GUI 24:00).
 * Food-request intervals still use {@code getGameTime() / 24000} unless noted otherwise.
 */
public final class EatProperlyConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_FAVOR_LEVEL;
    public static final ForgeConfigSpec.IntValue EXP_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue HP_BONUS_PER_LEVEL_AFTER_3;

    public static final ForgeConfigSpec.IntValue REQUEST_INTERVAL_DAYS_MIN;
    public static final ForgeConfigSpec.IntValue REQUEST_INTERVAL_DAYS_MAX;
    /**
     * One-shot migration marker for shipped request-interval defaults.
     * Existing worlds keep old toml values until this revision catches up; then min/max are
     * rewritten once and the file is saved.
     */
    public static final ForgeConfigSpec.IntValue REQUEST_INTERVAL_DEFAULTS_REVISION;
    public static final ForgeConfigSpec.IntValue REQUEST_NAG_INTERVAL_TICKS_MIN;
    public static final ForgeConfigSpec.IntValue REQUEST_NAG_INTERVAL_TICKS_MAX;
    public static final ForgeConfigSpec.IntValue REQUEST_ABANDON_DAYS;
    /**
     * One-shot migration marker for A8/A10 play defaults ({@code requestAbandonDays},
     * {@code hungryBingeUntilSatiety}). Only rewrites keys still stuck on the previous shipped
     * defaults; intentional player edits are kept.
     */
    public static final ForgeConfigSpec.IntValue PLAY_DEFAULTS_REVISION;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> REQUEST_SPEECH;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> THANKS_SPEECH;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ABANDON_SPEECH;
    /** Speech after a hypnosis note successfully forgets the current request. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HYPNOSIS_SPEECH;
    /** Bubble when Order Book filter leaves no foods to request. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EMPTY_MENU_SPEECH;
    /** Bubble when Order Book change forces a re-roll. Use {food}. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ORDER_BOOK_REMATCH_SPEECH;
    /** Bubble when low-satiety begging starts. Use {food}. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOW_SATIETY_BEG_SPEECH;
    /** Bubble when owner gives held food during low-satiety begging. Use {food}. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOW_SATIETY_BEG_THANKS_SPEECH;
    /** Bubble when owner gives a different pool food than the one that started begging. Use {food}. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOW_SATIETY_BEG_THANKS_OTHER_SPEECH;
    /** Bubble when player gives food outside the maid's effective request pool. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> OUT_OF_POOL_REFUSE_SPEECH;
    /** Bubble when maid enters anger / blocks GUI while angry. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> OUT_OF_POOL_ANGRY_SPEECH;
    /** Bubble when angry maid is forgiven by an in-pool food delivery. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> OUT_OF_POOL_FORGIVE_SPEECH;
    public static final ForgeConfigSpec.IntValue OUT_OF_POOL_WINDOW_INITIAL_TICKS;
    public static final ForgeConfigSpec.IntValue OUT_OF_POOL_WINDOW_EXTEND_TICKS;
    public static final ForgeConfigSpec.IntValue OUT_OF_POOL_ANGER_THRESHOLD;
    public static final ForgeConfigSpec.IntValue OUT_OF_POOL_ANGER_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue OUT_OF_POOL_GUI_BUBBLE_COOLDOWN_TICKS;

    public static final ForgeConfigSpec.DoubleValue SATIETY_DAILY_DRAIN_RATIO;
    public static final ForgeConfigSpec.IntValue SATIETY_LOW_THRESHOLD;
    /** Maid work/home meals only when satiety is strictly below this value. */
    public static final ForgeConfigSpec.IntValue EAT_WHEN_SATIETY_BELOW;
    /**
     * While satiety is strictly below this, bypass TLM work/home meal favor cooldowns
     * so the maid can keep eating until she reaches this value.
     */
    public static final ForgeConfigSpec.IntValue HUNGRY_BINGE_UNTIL_SATIETY;
    /** Ticks between heal-self meal eats after a successful bite. */
    public static final ForgeConfigSpec.IntValue HEAL_MEAL_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue HUNGER_SPEECH_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue FAVOR_DECAY_GRACE_DAYS;
    public static final ForgeConfigSpec.IntValue FAVOR_DECAY_PER_DAY_EXP;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HUNGER_SPEECH;

    public static final ForgeConfigSpec.BooleanValue SNACK_RAID_ENABLED;
    public static final ForgeConfigSpec.IntValue SNACK_RAID_RADIUS;
    public static final ForgeConfigSpec.IntValue SNACK_RAID_VERTICAL_RANGE;
    public static final ForgeConfigSpec.IntValue SNACK_RAID_SCAN_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue SNACK_RAID_MAX_CONTAINERS_PER_SCAN;
    public static final ForgeConfigSpec.IntValue SNACK_RAID_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SNACK_RAID_SPEECH;
    /** Bubble-only lines when approaching a container before raid. Use {maid}. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SNACK_RAID_APPROACH_SPEECH;
    public static final ForgeConfigSpec.IntValue FOOD_PERMIT_MAX_TAKE_BACKPACK;
    public static final ForgeConfigSpec.IntValue FOOD_PERMIT_MAX_TAKE_PICNIC;
    public static final ForgeConfigSpec.IntValue FOOD_PERMIT_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue FOOD_PERMIT_APPROACH_SPEED;
    public static final ForgeConfigSpec.IntValue FOOD_PERMIT_SEARCH_RADIUS;
    /** Bubble-only when starting a Food Permit fetch walk. Use {maid}. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FOOD_PERMIT_APPROACH_SPEECH;
    /** Bubble-only after a successful Food Permit extract. Use {maid}. */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FOOD_PERMIT_SPEECH;

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

    /** Shipped defaults for food-request spacing (also applied by one-shot config migration). */
    public static final int REQUEST_INTERVAL_DAYS_MIN_DEFAULT = 5;
    public static final int REQUEST_INTERVAL_DAYS_MAX_DEFAULT = 15;
    /** Bump when shipping a new pair of request-interval defaults that should overwrite old toml once. */
    public static final int REQUEST_INTERVAL_DEFAULTS_REVISION_CURRENT = 1;

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
                "Satiety day = TLM schedule day (dayTime, flips at GUI 24:00).",
                "Food-request day intervals still use getGameTime() / 24000."
        );

        builder.push("favorability");
        MAX_FAVOR_LEVEL = builder
                .comment(
                        "Maximum favorability level (vanilla max is 3). Default 100.",
                        "最大好感度等级（原版最大为3）。默认100。",
                        "如果玩家想提高好感度上限，可以增加此值。但请注意，过高的值可能导致游戏平衡问题。"
                )
                .defineInRange("maxFavorLevel", 100, 3, 1000);
        EXP_PER_LEVEL = builder
                .comment(
                        "Favorability points required for each level after level 3. Default 192.",
                        "3级之后每升一级所需的好感度点数。默认192。",
                        "增加此值会使升级更困难，减少则更容易。"
                )
                .defineInRange("expPerLevel", 192, 1, 100000);
        HP_BONUS_PER_LEVEL_AFTER_3 = builder
                .comment(
                        "Max health added per level after level 3. Default 2 (one heart).",
                        "3级之后每升一级增加的最大生命值。默认2（一颗心）。",
                        "设置为0则不增加生命值。"
                )
                .defineInRange("hpBonusPerLevelAfter3", 2, 0, 100);
        builder.pop();

        builder.push("foodRequest");
        REQUEST_INTERVAL_DAYS_MIN = builder
                .comment(
                        "Minimum game days between food requests. Default 5.",
                        "两次食物请求之间的最小游戏天数。默认5。",
                        "每次排期在最小～最大之间随机；同主人女仆还会错开至少一天。"
                )
                .defineInRange("requestIntervalDaysMin", REQUEST_INTERVAL_DAYS_MIN_DEFAULT, 1, 365);
        REQUEST_INTERVAL_DAYS_MAX = builder
                .comment(
                        "Maximum game days between food requests. Default 15.",
                        "两次食物请求之间的最大游戏天数。默认15（典型约10天）。",
                        "窗内被同主人其他女仆占满时，会继续往后找空日。"
                )
                .defineInRange("requestIntervalDaysMax", REQUEST_INTERVAL_DAYS_MAX_DEFAULT, 1, 365);
        REQUEST_INTERVAL_DEFAULTS_REVISION = builder
                .comment(
                        "Internal migration marker for request-interval defaults. Do not edit.",
                        "索要间隔默认值的内部迁移标记。一般无需修改。",
                        "升级模组时若小于当前版本，会自动把间隔默认覆盖为最新默认并写回配置文件。"
                )
                .defineInRange("requestIntervalDefaultsRevision", 0, 0, 100);
        REQUEST_NAG_INTERVAL_TICKS_MIN = builder
                .comment(
                        "Minimum ticks between request chat reminders. Default 15000.",
                        "请求聊天提醒之间的最小tick数。默认15000（约12.5秒）。",
                        "增加此值会使提醒频率降低。"
                )
                .defineInRange("requestNagIntervalTicksMin", 15000, 200, 720000);
        REQUEST_NAG_INTERVAL_TICKS_MAX = builder
                .comment(
                        "Maximum ticks between request chat reminders. Default 28000.",
                        "Each reminder rolls a new random delay in [min, max].",
                        "请求聊天提醒之间的最大tick数。默认28000（约23.3秒）。",
                        "每次提醒会在最小和最大值之间随机选择新的延迟。"
                )
                .defineInRange("requestNagIntervalTicksMax", 28000, 200, 720000);
        REQUEST_ABANDON_DAYS = builder
                .comment(
                        "Game days without fulfillment before the maid gives up the request. Default 10.",
                        "女仆放弃未满足请求前的天数。默认10。",
                        "增加此值会使女仆等待更久才放弃。"
                )
                .defineInRange("requestAbandonDays", PlayDefaultsMigration.REQUEST_ABANDON_DAYS_DEFAULT, 1, 365);
        PLAY_DEFAULTS_REVISION = builder
                .comment(
                        "Internal migration marker for play defaults (abandon days / binge stop line). Do not edit.",
                        "游玩默认值（放弃天数 / 连吃停线）的内部迁移标记。一般无需修改。",
                        "升级时若仍是旧默认（abandon=5 / binge=8）会一次性改为 10；玩家改过的值不覆盖。"
                )
                .defineInRange("playDefaultsRevision", 0, 0, 100);
        REQUEST_SPEECH = builder
                .comment(
                        "Speech templates when requesting food. Use {food} for food name.",
                        "请求食物时的语音模板。使用{food}表示食物名称。",
                        "可以添加多条，随机选择。"
                )
                .defineList("requestSpeech", List.of(
                        "主人，我想吃{food}，好不好嘛",
                        "诶嘿……可以给我{food}吗？",
                        "主人～想吃{food}！",
                        "拜托拜托，想要{food}",
                        "今天想吃{food}呢"
                ), o -> o instanceof String);
        THANKS_SPEECH = builder
                .comment(
                        "Speech templates when request is completed. Use {food} for food name.",
                        "请求完成时的语音模板。使用{food}表示食物名称。",
                        "可以添加多条，随机选择。"
                )
                .defineList("thanksSpeech", List.of(
                        "哇，主人真的给我做了{food}，谢谢你主人！",
                        "收到{food}啦，好开心！",
                        "谢谢主人的{food}～",
                        "嗯嗯，{food}很好吃的样子！",
                        "有{food}真幸福，最喜欢主人了"
                ), o -> o instanceof String);
        ABANDON_SPEECH = builder
                .comment(
                        "Speech when giving up an unfulfilled request. Use {food} for food name.",
                        "放弃未满足请求时的语音。使用{food}表示食物名称。",
                        "可以添加多条，随机选择。"
                )
                .defineList("abandonSpeech", List.of(
                        "主人很忙对吗？我明白的，提出这样的请求很抱歉……",
                        "没事的，主人不用在意……只是有点想吃{food}而已",
                        "看来这次是我太任性了……对不起，主人",
                        "呜呜……没关系的，主人一定有更重要的事要做吧",
                        "我还是自己忍一下好了……抱歉打扰你了，主人"
                ), o -> o instanceof String);
        HYPNOSIS_SPEECH = builder
                .comment(
                        "Speech after a hypnosis note makes the maid forget her current food request.",
                        "催眠笔记使女仆忘记当前食物请求后的语音。",
                        "可以添加多条，随机选择。"
                )
                .defineList("hypnosisSpeech", List.of(
                        "诶？我想吃什么来着……",
                        "脑袋晕乎乎的……今天想吃啥忘记了……",
                        "唔……点单？好像想不起来了……"
                ), o -> o instanceof String);
        EMPTY_MENU_SPEECH = builder
                .comment(
                        "Bubble when the Order Book leaves an empty filtered food pool (no request started).",
                        "点餐本过滤后菜单为空、无法发起索要时的气泡话术。",
                        "可以添加多条，随机选择。"
                )
                .defineList("emptyMenuSpeech", List.of(
                        "菜单上一个菜都没有，让我怎么选嘛……",
                        "诶？菜单是空的诶，点不了餐啦",
                        "没有菜的菜单算什么菜单嘛，我才不要！",
                        "今天的菜单……什么都没有？",
                        "什么啊，菜单上一个菜都没有让我怎么选嘛"
                ), o -> o instanceof String);
        ORDER_BOOK_REMATCH_SPEECH = builder
                .comment(
                        "Bubble when Order Book changes invalidate the current request and a new food is picked. Use {food}.",
                        "点餐本变更导致当前索要不入池、立刻重抽时的气泡。使用{food}表示新食物。",
                        "可以添加多条，随机选择。"
                )
                .defineList("orderBookRematchSpeech", List.of(
                        "原来这个菜不在点餐本上吗？我看看……吃{food}好了！",
                        "诶，点餐本改过了？那我换成{food}吧～",
                        "刚才那道不在菜单上啦……那我想吃{food}！",
                        "菜单更新了吗？嗯嗯，这次要{food}",
                        "咦，那道菜划掉了？那我改要{food}！"
                ), o -> o instanceof String);
        LOW_SATIETY_BEG_SPEECH = builder
                .comment(
                        "Bubble once when low-satiety begging starts. Use {food} for the owner's held food.",
                        "低饱食祈求开始时发一次的气泡。使用{food}表示主人手中的食物。",
                        "可以添加多条，随机选择。"
                )
                .defineList("lowSatietyBegSpeech", List.of(
                        "主人…可以吗？",
                        "主人手里拿的难道是{food}？可以让我尝尝吗？",
                        "诶嘿，那是{food}吧？分我一点嘛～",
                        "主人故意拿{food}晃我看的吧？太坏啦"
                ), o -> o instanceof String);
        LOW_SATIETY_BEG_THANKS_SPEECH = builder
                .comment(
                        "Bubble once when the owner gives the same food that started low-satiety begging. Use {food}.",
                        "低饱食祈求中主人递上「刚才提过的那份」食物时发一次的气泡。使用{food}表示食物名称。",
                        "可以添加多条，随机选择。"
                )
                .defineList("lowSatietyBegThanksSpeech", List.of(
                        "万岁！谢谢你主人！",
                        "嘿嘿，我就知道主人最好了！",
                        "谢谢主人！我现在就……（咽口水）"
                ), o -> o instanceof String);
        LOW_SATIETY_BEG_THANKS_OTHER_SPEECH = builder
                .comment(
                        "Bubble when the owner gives a different pool food than the one that started begging. Use {food}.",
                        "低饱食祈求中主人改递「别的」池内食物时发一次的气泡。使用{food}表示实际递上的食物。",
                        "可以添加多条，随机选择。"
                )
                .defineList("lowSatietyBegThanksOtherSpeech", List.of(
                        "欸？这个吗，也好啦，谢谢你主人！",
                        "诶，不是刚才那个……不过这个也不错，谢谢主人！"
                ), o -> o instanceof String);
        OUT_OF_POOL_REFUSE_SPEECH = builder
                .comment(
                        "Bubble when the owner gives food outside the maid's effective request pool.",
                        "主人投递有效索要池外食物时的拒绝气泡。",
                        "可以添加多条，随机选择。"
                )
                .defineList("outOfPoolRefuseSpeech", List.of(
                        "这是什么啊，不要吃这个嘛，不要不要不要！",
                        "诶——这个我才不要吃啦！",
                        "呜，这个看起来好奇怪……不要给我嘛",
                        "主人坏！这个绝对不能进肚子！"
                ), o -> o instanceof String);
        OUT_OF_POOL_ANGRY_SPEECH = builder
                .comment(
                        "Bubble when the maid enters anger or blocks GUI while angry.",
                        "女仆因池外投递过多进入生气、或生气中挡开 GUI 时的气泡。",
                        "可以添加多条，随机选择。"
                )
                .defineList("outOfPoolAngrySpeech", List.of(
                        "主人一直给我塞不好吃的东西，不理主人了！",
                        "哼！我真的生气啦！",
                        "不要不要，讨厌！主人坏！"
                ), o -> o instanceof String);
        OUT_OF_POOL_FORGIVE_SPEECH = builder
                .comment(
                        "Bubble when an angry maid is forgiven by an in-pool food delivery.",
                        "生气中主人投递有效池内食物、女仆原谅时的气泡。",
                        "可以添加多条，随机选择。"
                )
                .defineList("outOfPoolForgiveSpeech", List.of(
                        "我就知道主人还是喜欢我的！我闹脾气了，对不起，主人...",
                        "嘿嘿，原谅你啦……下次不要再塞奇怪的东西哦",
                        "果然还是主人最好了……刚才是我太任性了",
                        "消气消气！有好吃的就不生气啦！"
                ), o -> o instanceof String);
        OUT_OF_POOL_WINDOW_INITIAL_TICKS = builder
                .comment(
                        "Accumulation window opened by the first out-of-pool stow (game ticks). Default 2000 (~100s).",
                        "首次池外进包开启的累计窗口（游戏 tick）。默认 2000（约 100 秒）。"
                )
                .defineInRange("outOfPoolWindowInitialTicks", 2000, 1, 240000);
        OUT_OF_POOL_WINDOW_EXTEND_TICKS = builder
                .comment(
                        "Extra ticks added to the window for each further successful out-of-pool stow. Default 1000.",
                        "窗口未耗尽时每成功进包一份池外食物延长的 tick。默认 1000。"
                )
                .defineInRange("outOfPoolWindowExtendTicks", 1000, 0, 240000);
        OUT_OF_POOL_ANGER_THRESHOLD = builder
                .comment(
                        "Out-of-pool stow events within the window before anger (each batch/put = 1, not stack size). Default 10.",
                        "累计窗口内池外投递次数达到此值时生气（每次批量放入只算 1，不按个数）。默认 10。"
                )
                .defineInRange("outOfPoolAngerThreshold", 10, 1, 1000);
        OUT_OF_POOL_ANGER_DURATION_TICKS = builder
                .comment(
                        "Anger duration / refresh length (game ticks). Default 12000 (~10 real minutes).",
                        "生气持续与刷新时长（游戏 tick）。默认 12000（约现实 10 分钟）。"
                )
                .defineInRange("outOfPoolAngerDurationTicks", 12000, 1, 240000);
        OUT_OF_POOL_GUI_BUBBLE_COOLDOWN_TICKS = builder
                .comment(
                        "Cooldown between angry bubbles when blocking maid GUI. Default 80 (4s).",
                        "生气中挡开 GUI 时生气气泡的冷却。默认 80（4 秒）。"
                )
                .defineInRange("outOfPoolGuiBubbleCooldownTicks", 80, 0, 12000);
        builder.pop();

        builder.push("satiety");
        SATIETY_DAILY_DRAIN_RATIO = builder
                .comment(
                        "Fraction of max satiety (20) drained each TLM schedule day at 24:00 (dayTime tod 18000). Default 0.20 (4 points).",
                        "每个车万日程日在 24:00（dayTime tod 18000）结算时消耗的最大饱食度（20）比例。默认0.20（4点）。",
                        "设置为0则不消耗饱食度。"
                )
                .defineInRange("satietyDailyDrainRatio", 0.20D, 0.0D, 1.0D);
        SATIETY_LOW_THRESHOLD = builder
                .comment(
                        "Below this satiety, hunger speech and favor-decay grace counting start. Default 6.",
                        "低于此饱食度时，饥饿语音和好感度衰减宽限期开始计算。默认6。",
                        "增加此值会使女仆更容易感到饥饿。"
                )
                .defineInRange("satietyLowThreshold", 6, 0, 20);
        EAT_WHEN_SATIETY_BELOW = builder
                .comment(
                        "Work/home/heal-self meals really eat only when satiety < this. If cooldown fires but satiety",
                        "is at/above this, fake-skip (work/home) or skip (heal-self): apply TLM meal cooldown with 0 favor",
                        "(no food, no sound, no empty-meal bubble for work/home). Default 16 (skip when satiety > 15).",
                        "工作餐/家餐/自愈餐仅在饱食度低于此值时真正进食。冷却到期但饱食度已达此值及以上时：",
                        "工作餐/家餐假跳过（仅写入TLM餐冷却且好感+0）；自愈餐直接跳过。默认16（即>15不真吃）。"
                )
                .defineInRange("eatWhenSatietyBelow", 16, 0, 21);
        HUNGRY_BINGE_UNTIL_SATIETY = builder
                .comment(
                        "While satiety < this, ignore TLM WorkMeal/HomeMeal favor cooldowns so the maid",
                        "can binge-eat until she reaches this value. Between this and eatWhenSatietyBelow,",
                        "normal TLM cooldowns apply. Default 10.",
                        "当饱食度低于此值时，忽略TLM工作餐/家庭餐的好感度冷却，女仆可以连续进食直到达到此值。默认10。",
                        "在此值与eatWhenSatietyBelow之间，正常TLM冷却生效。"
                )
                .defineInRange("hungryBingeUntilSatiety",
                        PlayDefaultsMigration.HUNGRY_BINGE_UNTIL_SATIETY_DEFAULT, 0, 20);
        HEAL_MEAL_COOLDOWN_TICKS = builder
                .comment(
                        "Ticks between heal-self meal eats after a successful bite. Matches TLM WorkMeal cooldown by default.",
                        "自愈餐成功进食后的间隔 tick。默认3600（与TLM工作餐冷却一致）。"
                )
                .defineInRange("healMealCooldownTicks",
                        com.touhoumaid.eatproperly.satiety.HealMealPolicy.DEFAULT_COOLDOWN_TICKS, 0, 720000);
        HUNGER_SPEECH_INTERVAL_TICKS = builder
                .comment(
                        "Ticks between hunger chat/bubble while satiety is low. Default 600 (30 seconds).",
                        "饱食度低时，饥饿聊天/气泡之间的tick数。默认600（30秒）。",
                        "增加此值会使饥饿提醒频率降低。"
                )
                .defineInRange("hungerSpeechIntervalTicks", 600, 200, 720000);
        FAVOR_DECAY_GRACE_DAYS = builder
                .comment(
                        "Game days of continuous low satiety before favor starts decaying. Default 5.",
                        "持续低饱食度后，好感度开始衰减的天数。默认5。",
                        "增加此值会使女仆有更多时间恢复饱食度。"
                )
                .defineInRange("favorDecayGraceDays", 5, 0, 365);
        FAVOR_DECAY_PER_DAY_EXP = builder
                .comment(
                        "Favor points removed per day after grace while still low. Default -1 = half of expPerLevel.",
                        "宽限期后，饱食度仍低时每天移除的好感度点数。默认-1 = expPerLevel的一半。",
                        "设置为0则不衰减。"
                )
                .defineInRange("favorDecayPerDayExp", -1, -1, 100000);
        HUNGER_SPEECH = builder
                .comment(
                        "Speech templates when satiety is low. Use {maid} for maid display name.",
                        "饱食度低时的语音模板。使用{maid}表示女仆显示名称。",
                        "可以添加多条，随机选择。"
                )
                .defineList("hungerSpeech", List.of(
                        "{maid}有点饿了……",
                        "肚子咕咕叫了，主人……",
                        "好饿呀，想吃点东西",
                        "主人，{maid}快要饿扁了",
                        "再不吃饭，{maid}要没有力气了",
                        "真是的，明明说好有好吃的才跟来的啊，肚子饿了啊——太过分啦！",
                        "那个，我肚子饿了呀",
                        "肚子好饿，肚子好饿啊~"
                ), o -> o instanceof String);
        builder.pop();

        builder.push("snackRaid");
        SNACK_RAID_ENABLED = builder
                .comment(
                        "When true, hungry maids with no backpack food may sneak food from nearby containers.",
                        "为true时，饥饿且背包没有食物的女仆可能会从附近的容器中偷取食物。",
                        "设置为false则禁用此功能。"
                )
                .define("snackRaidEnabled", true);
        SNACK_RAID_RADIUS = builder
                .comment(
                        "Horizontal search radius around the maid (blocks). Default 10.",
                        "女仆周围水平搜索半径（方块）。默认10。",
                        "增加此值会使女仆搜索更大范围。"
                )
                .defineInRange("snackRaidRadius", 10, 4, 24);
        SNACK_RAID_VERTICAL_RANGE = builder
                .comment(
                        "Vertical search range above/below the maid (blocks). Default 3.",
                        "女仆上下垂直搜索范围（方块）。默认3。",
                        "增加此值会使女仆搜索更高或更低的位置。"
                )
                .defineInRange("snackRaidVerticalRange", 3, 1, 8);
        SNACK_RAID_SCAN_INTERVAL_TICKS = builder
                .comment(
                        "Ticks between container scans per maid (staggered by entity id). Default 80.",
                        "每个女仆容器扫描之间的tick数（按实体ID错开）。默认80。",
                        "增加此值会降低扫描频率。"
                )
                .defineInRange("snackRaidScanIntervalTicks", 80, 20, 600);
        SNACK_RAID_MAX_CONTAINERS_PER_SCAN = builder
                .comment(
                        "Max container block entities checked per scan. Default 12.",
                        "每次扫描检查的最大容器方块实体数。默认12。",
                        "增加此值会使每次扫描检查更多容器。"
                )
                .defineInRange("snackRaidMaxContainersPerScan", 12, 1, 64);
        SNACK_RAID_COOLDOWN_TICKS = builder
                .comment(
                        "Ticks after a successful raid before another. Default 24000 (1 game day).",
                        "成功掠夺后，再次掠夺前的tick数。默认24000（1个游戏日）。",
                        "增加此值会使女仆更少偷吃。"
                )
                .defineInRange("snackRaidCooldownTicks", 24000, 200, 720000);
        SNACK_RAID_SPEECH = builder
                .comment(
                        "Bubble-only lines after a successful raid. Use {maid} for display name. No public chat.",
                        "成功掠夺后的气泡文本。使用{maid}表示女仆显示名称。不会公开聊天。",
                        "可以添加多条，随机选择。"
                )
                .defineList("snackRaidSpeech", List.of(
                        "唔…好饿啊，{maid}只能自力更生了！",
                        "就一小口…主人一定不会数的对吧？",
                        "这不叫偷吃，叫紧急自助！",
                        "主人要是问起…就说风把饼干吹进嘴里的！"
                ), o -> o instanceof String);
        SNACK_RAID_APPROACH_SPEECH = builder
                .comment(
                        "Bubble-only lines when approaching a container before raid. Use {maid} for display name.",
                        "接近容器准备掠夺时的气泡文本。使用{maid}表示女仆显示名称。",
                        "可以添加多条，随机选择。"
                )
                .defineList("snackRaidApproachSpeech", List.of(
                        "看不到我...看不到我...",
                        "嘘...主人不会发现的...",
                        "悄悄的...悄悄的...",
                        "嘘！别出声，我在执行秘密任务"
                ), o -> o instanceof String);
        builder.pop();

        builder.push("foodPermit");
        FOOD_PERMIT_MAX_TAKE_BACKPACK = builder
                .comment(
                        "Max effective-pool food items taken into backpack per Food Permit fetch. Default 5.",
                        "取粮许可每次取到女仆背包的有效池食物上限。默认5。"
                )
                .defineInRange("foodPermitMaxTakeBackpack", 5, 1, 64);
        FOOD_PERMIT_MAX_TAKE_PICNIC = builder
                .comment(
                        "Max effective-pool food items taken into picnic basket during rest refill. Default 16.",
                        "休息时补野餐篮，取粮许可每次取到野餐篮的有效池食物上限。默认16。"
                )
                .defineInRange("foodPermitMaxTakePicnic", 16, 1, 64);
        FOOD_PERMIT_COOLDOWN_TICKS = builder
                .comment(
                        "Shared cooldown after a successful Food Permit fetch. Default 600 (30 seconds).",
                        "取粮许可成功取粮后的共享冷却。默认600（30秒）。"
                )
                .defineInRange("foodPermitCooldownTicks", 600, 0, 720000);
        FOOD_PERMIT_APPROACH_SPEED = builder
                .comment(
                        "Walk speed modifier while openly fetching with a Food Permit. Snack raids sneak at 0.2. Default 0.35.",
                        "戴取粮许可大方取粮时的接近速度倍率。偷吃为0.2。默认0.35。"
                )
                .defineInRange("foodPermitApproachSpeed", 0.35D, 0.2D, 0.6D);
        FOOD_PERMIT_SEARCH_RADIUS = builder
                .comment(
                        "Horizontal search radius for marked containers (blocks). Wider than snack raids. Default 16.",
                        "寻找已标记容器的水平搜索半径（方块）。比偷吃范围更大。默认16。"
                )
                .defineInRange("foodPermitSearchRadius", 16, 4, 48);
        FOOD_PERMIT_APPROACH_SPEECH = builder
                .comment(
                        "Bubble-only lines once when starting a Food Permit fetch walk. Use {maid} for display name.",
                        "取粮许可开始走向标记箱时只说一次的气泡文本。使用{maid}表示女仆显示名称。",
                        "可以添加多条，随机选择。空列表关闭接近气泡。"
                )
                .defineList("foodPermitApproachSpeech", List.of(
                        "{maid}去拿点吃的，马上回来～",
                        "补给一下~补给一下~",
                        "今天吃什么好呢？让我看看"
                ), o -> o instanceof String);
        FOOD_PERMIT_SPEECH = builder
                .comment(
                        "Bubble-only lines after a successful Food Permit extract. Use {maid} for display name.",
                        "取粮许可成功取到食物后的气泡文本。使用{maid}表示女仆显示名称。",
                        "可以添加多条，随机选择。空列表关闭成功气泡。"
                )
                .defineList("foodPermitSpeech", List.of(
                        "补给大成功！",
                        "好～拿到啦！",
                        "哇啊，这么多好吃的，向伟大的主人敬礼~"
                ), o -> o instanceof String);
        builder.pop();

        builder.push("milestones");
        MILESTONES = builder
                .comment(
                        "Optional legacy milestone override table strings. Default empty; not required for rewards to work.",
                        "Milestone reward levels and pools are already active and hardcoded (see RewardPoolTables);",
                        "this list is reserved for possible future per-level overrides.",
                        "可选的旧版里程碑覆盖表字符串。默认为空；奖励工作不需要此项。",
                        "里程碑奖励等级和池已激活并硬编码（见RewardPoolTables）；",
                        "此列表保留用于将来可能的每个等级覆盖。"
                )
                .defineListAllowEmpty("milestones", ArrayList::new, o -> o instanceof String);
        MILESTONE_GIFT_SPEECH = builder
                .comment(
                        "Said once per new pending gift (chat + bubble). Use {maid} for display name.",
                        "每个新待处理礼物说一次（聊天+气泡）。使用{maid}表示女仆显示名称。",
                        "可以添加多条，随机选择。"
                )
                .defineList("milestoneGiftSpeech", List.of(
                        "我有东西给你哦，主人~",
                        "诶嘿……给主人准备了小礼物，记得来拿呀～",
                        "主人主人！我藏了一样东西给你哦",
                        "尾巴都摇起来了……因为有礼物要送给主人！",
                        "唔，有点害羞……但我真的想把这个交给主人",
                        "叮咚——心意快递到啦，请主人收下",
                        "今天也要好好吃饭，还有……请收下我的一点小心意吧",
                        "我认真想了很久才选的！快来领走它～"
                ), o -> o instanceof String);
        MILESTONE_GIFT_BUBBLE = builder
                .comment(
                        "Repeating bubble while unclaimed gifts remain. Use {maid}. Keep short.",
                        "未领取礼物存在时重复的气泡。使用{maid}。保持简短。",
                        "可以添加多条，随机选择。"
                )
                .defineList("milestoneGiftBubble", List.of(
                        "我有东西给你哦，主人~",
                        "主人，过来一下嘛～"
                ), o -> o instanceof String);
        MILESTONE_EMPTY_SPEECH = builder
                .comment(
                        "When a milestone fires but the pool has nothing left to give. Use {maid}.",
                        "当里程碑触发但奖池没有东西可送时。使用{maid}表示女仆显示名称。",
                        "可以添加多条，随机选择。"
                )
                .defineList("milestoneEmptySpeech", List.of(
                        "想送你礼物来着……可是好像没有了呢",
                        "欸？口袋空空的……下次一定准备好！",
                        "礼物清单翻到底了……只剩谢谢你了，主人",
                        "呜，想送礼却拿不出新东西……抱一下可以吗？"
                ), o -> o instanceof String);
        builder.pop();

        SPEC = builder.build();
    }

    /**
     * If the on-disk revision is behind, overwrite request-interval min/max with the shipped
     * defaults and persist the file. Safe to call on every config load/reload; runs at most once
     * per revision bump. Returns true when values were written.
     */
    public static boolean migrateRequestIntervalDefaultsIfNeeded() {
        if (!SPEC.isLoaded()) {
            return false;
        }
        int revision = REQUEST_INTERVAL_DEFAULTS_REVISION.get();
        if (revision >= REQUEST_INTERVAL_DEFAULTS_REVISION_CURRENT) {
            return false;
        }
        REQUEST_INTERVAL_DAYS_MIN.set(REQUEST_INTERVAL_DAYS_MIN_DEFAULT);
        REQUEST_INTERVAL_DAYS_MAX.set(REQUEST_INTERVAL_DAYS_MAX_DEFAULT);
        REQUEST_INTERVAL_DEFAULTS_REVISION.set(REQUEST_INTERVAL_DEFAULTS_REVISION_CURRENT);
        SPEC.save();
        return true;
    }

    /**
     * Bumps A8/A10 shipped defaults once when the on-disk revision is behind.
     * Only rewrites keys still equal to the previous defaults (5 / 8); custom values stay.
     * Returns true when any value was rewritten (revision is always advanced when behind).
     */
    public static boolean migratePlayDefaultsIfNeeded() {
        if (!SPEC.isLoaded()) {
            return false;
        }
        PlayDefaultsMigration.Decision decision = PlayDefaultsMigration.decide(
                PLAY_DEFAULTS_REVISION.get(),
                REQUEST_ABANDON_DAYS.get(),
                HUNGRY_BINGE_UNTIL_SATIETY.get());
        if (!decision.shouldBumpRevision()) {
            return false;
        }
        if (decision.migrateAbandonDays()) {
            REQUEST_ABANDON_DAYS.set(decision.abandonDaysAfter());
        }
        if (decision.migrateBingeUntil()) {
            HUNGRY_BINGE_UNTIL_SATIETY.set(decision.bingeUntilAfter());
        }
        PLAY_DEFAULTS_REVISION.set(PlayDefaultsMigration.REVISION_CURRENT);
        SPEC.save();
        return decision.migrateAbandonDays() || decision.migrateBingeUntil();
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
