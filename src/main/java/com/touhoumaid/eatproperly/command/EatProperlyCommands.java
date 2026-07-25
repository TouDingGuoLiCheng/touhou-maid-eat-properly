package com.touhoumaid.eatproperly.command;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.debug.DebugOverrides;
import com.touhoumaid.eatproperly.favorability.FavorabilityExtension;
import com.touhoumaid.eatproperly.food.FoodPool;
import com.touhoumaid.eatproperly.food.FoodRequestData;
import com.touhoumaid.eatproperly.food.FoodRequestHandler;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

public final class EatProperlyCommands {
    private static final SimpleCommandExceptionType NO_MAID =
            new SimpleCommandExceptionType(Component.literal("附近没有找到车万女仆（或目标不是女仆）"));

    private EatProperlyCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("eatproperly")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status")
                        .executes(ctx -> status(ctx, null))
                        .then(Commands.argument("maid", EntityArgument.entity())
                                .executes(ctx -> status(ctx, EntityArgument.getEntity(ctx, "maid")))))
                .then(Commands.literal("request")
                        .then(Commands.literal("now")
                                .executes(ctx -> requestNow(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> requestNow(ctx, EntityArgument.getEntity(ctx, "maid")))))
                        .then(Commands.literal("clear")
                                .executes(ctx -> requestClear(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> requestClear(ctx, EntityArgument.getEntity(ctx, "maid")))))
                        .then(Commands.literal("in")
                                .then(Commands.argument("days", IntegerArgumentType.integer(0, 365))
                                        .executes(ctx -> requestInDays(ctx, null))
                                        .then(Commands.argument("maid", EntityArgument.entity())
                                                .executes(ctx -> requestInDays(ctx, EntityArgument.getEntity(ctx, "maid"))))))
                        .then(Commands.literal("interval")
                                .then(Commands.literal("clear")
                                        .executes(EatProperlyCommands::clearRequestIntervalOverride))
                                .then(Commands.argument("min", IntegerArgumentType.integer(0, 365))
                                        .then(Commands.argument("max", IntegerArgumentType.integer(0, 365))
                                                .executes(EatProperlyCommands::setRequestIntervalOverride))))
                        .then(Commands.literal("age")
                                .then(Commands.argument("days", IntegerArgumentType.integer(0, 365))
                                        .executes(ctx -> requestAge(ctx, null))
                                        .then(Commands.argument("maid", EntityArgument.entity())
                                                .executes(ctx -> requestAge(ctx, EntityArgument.getEntity(ctx, "maid"))))))
                        .then(Commands.literal("abandon")
                                .executes(ctx -> requestAbandon(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> requestAbandon(ctx, EntityArgument.getEntity(ctx, "maid"))))))
                .then(Commands.literal("nag")
                        .then(Commands.literal("now")
                                .executes(ctx -> nagNow(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> nagNow(ctx, EntityArgument.getEntity(ctx, "maid")))))
                        .then(Commands.literal("interval")
                                .then(Commands.literal("clear")
                                        .executes(EatProperlyCommands::clearNagIntervalOverride))
                                .then(Commands.argument("min", IntegerArgumentType.integer(1, 720000))
                                        .then(Commands.argument("max", IntegerArgumentType.integer(1, 720000))
                                                .executes(EatProperlyCommands::setNagIntervalOverride)))))
                .then(Commands.literal("satiety")
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0, EatProperlyConfig.SATIETY_MAX))
                                        .executes(ctx -> satietySet(ctx, null))
                                        .then(Commands.argument("maid", EntityArgument.entity())
                                                .executes(ctx -> satietySet(ctx, EntityArgument.getEntity(ctx, "maid"))))))
                        .then(Commands.literal("drain")
                                .executes(ctx -> satietyDrain(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> satietyDrain(ctx, EntityArgument.getEntity(ctx, "maid")))))
                        .then(Commands.literal("lowdays")
                                .then(Commands.argument("days", IntegerArgumentType.integer(0, 365))
                                        .executes(ctx -> lowDaysSet(ctx, null))
                                        .then(Commands.argument("maid", EntityArgument.entity())
                                                .executes(ctx -> lowDaysSet(ctx, EntityArgument.getEntity(ctx, "maid")))))))
                .then(Commands.literal("favor")
                        .then(Commands.literal("setlevel")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 1000))
                                        .executes(ctx -> favorSetLevel(ctx, null))
                                        .then(Commands.argument("maid", EntityArgument.entity())
                                                .executes(ctx -> favorSetLevel(ctx, EntityArgument.getEntity(ctx, "maid")))))))
                .then(Commands.literal("override")
                        .then(Commands.literal("clear")
                                .executes(EatProperlyCommands::clearAllOverrides))
                        .then(Commands.literal("status")
                                .executes(EatProperlyCommands::overrideStatus)));

        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(root);
        dispatcher.register(Commands.literal("ep")
                .requires(source -> source.hasPermission(2))
                .redirect(node));
    }

    private static EntityMaid resolveMaid(CommandSourceStack source, Entity explicit) throws CommandSyntaxException {
        if (explicit instanceof EntityMaid maid) {
            return maid;
        }
        if (explicit != null) {
            throw NO_MAID.create();
        }
        ServerPlayer player = source.getPlayerOrException();
        AABB box = player.getBoundingBox().inflate(16.0D);
        List<EntityMaid> list = player.level().getEntitiesOfClass(EntityMaid.class, box);
        return list.stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElseThrow(NO_MAID::create);
    }

    private static int status(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        FoodRequestData.setInitialized(maid);
        SatietyData.ensureInitialized(maid);

        long day = FoodRequestData.currentGameDay(maid);
        ItemStack want = FoodRequestData.getRequestStack(maid);
        String wantName = want.isEmpty() ? "(无)" : want.getHoverName().getString();
        FavorabilityManager fav = maid.getFavorabilityManager();

        long startDay = want.isEmpty() ? -1L : FoodRequestData.getRequestStartDay(maid);
        long abandonIn = want.isEmpty() ? -1L : FoodRequestData.daysUntilAbandon(maid);
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                "[EatProperly] %s\n" +
                        "  day=%d nextRequestDay=%d request=%s startDay=%s abandonIn=%s abandonDays=%d\n" +
                        "  satiety=%d/%d lowDays=%d lastSatietyDay=%d\n" +
                        "  favorLv=%d points=%d nextNeed=%d hp=%.1f\n" +
                        "  cfgRequestDays=%d-%d cfgNagTicks=%d-%d\n" +
                        "  overrideRequestDays=%s overrideNagTicks=%s",
                maid.getDisplayName().getString(),
                day,
                FoodRequestData.getNextRequestDay(maid),
                wantName,
                startDay < 0 ? "-" : Long.toString(startDay),
                abandonIn < 0 ? "-" : Long.toString(abandonIn),
                EatProperlyConfig.REQUEST_ABANDON_DAYS.get(),
                SatietyData.getSatiety(maid),
                EatProperlyConfig.SATIETY_MAX,
                SatietyData.getLowSatietyDays(maid),
                SatietyData.getLastSatietyDay(maid),
                fav.getLevel(),
                maid.getFavorability(),
                fav.nextLevelPoint(),
                maid.getMaxHealth(),
                EatProperlyConfig.REQUEST_INTERVAL_DAYS_MIN.get(),
                EatProperlyConfig.REQUEST_INTERVAL_DAYS_MAX.get(),
                EatProperlyConfig.REQUEST_NAG_INTERVAL_TICKS_MIN.get(),
                EatProperlyConfig.REQUEST_NAG_INTERVAL_TICKS_MAX.get(),
                formatRange(DebugOverrides.requestDaysMin(), DebugOverrides.requestDaysMax()),
                formatRange(DebugOverrides.nagTicksMin(), DebugOverrides.nagTicksMax())
        )), false);
        return 1;
    }

    private static String formatRange(java.util.OptionalInt min, java.util.OptionalInt max) {
        if (min.isEmpty()) {
            return "(无)";
        }
        return min.getAsInt() + "-" + max.orElse(min.getAsInt());
    }

    private static int requestNow(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        FoodRequestData.setInitialized(maid);
        FoodRequestData.clearRequest(maid);
        FoodRequestData.setNextRequestDay(maid, FoodRequestData.currentGameDay(maid));
        // Trigger immediately: pick and set request
        ItemStack pick = FoodPool.pickRandom(maid.getRandom());
        if (pick.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("食物池为空，无法生成索要"));
            return 0;
        }
        FoodRequestData.setRequest(maid, pick);
        String text = MaidSpeech.formatRequest(pick, maid.getRandom());
        if (!text.isEmpty()) {
            MaidSpeech.speakToOwner(maid, text);
        }
        // Mark nag as just done so the next tick's maybeNag does not repeat the line.
        FoodRequestData.setLastNagGameTime(maid, maid.level().getGameTime());
        FoodRequestData.setNextNagIntervalTicks(maid,
                FoodRequestData.randomNagIntervalTicks(maid.getRandom()));
        ItemStack finalPick = pick;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "已立即索要: " + finalPick.getHoverName().getString()), false);
        return 1;
    }

    private static int requestClear(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        FoodRequestData.clearRequest(maid);
        long day = FoodRequestData.currentGameDay(maid);
        int interval = FoodRequestData.randomIntervalDays(maid.getRandom());
        FoodRequestData.setNextRequestDay(maid, day + interval);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "已清除索要，下次约 " + interval + " 日后 (day=" + (day + interval) + ")"), true);
        return 1;
    }

    private static int requestInDays(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        int days = IntegerArgumentType.getInteger(ctx, "days");
        FoodRequestData.setInitialized(maid);
        FoodRequestData.clearRequest(maid);
        long target = FoodRequestData.currentGameDay(maid) + days;
        FoodRequestData.setNextRequestDay(maid, target);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "下次索要设为 " + days + " 日后 (day=" + target + ")"), false);
        return 1;
    }

    private static int requestAge(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        if (!FoodRequestData.hasRequest(maid)) {
            ctx.getSource().sendFailure(Component.literal("当前没有进行中的索要，可先 /ep request now"));
            return 0;
        }
        int days = IntegerArgumentType.getInteger(ctx, "days");
        FoodRequestData.setRequestAgeDays(maid, days);
        if (FoodRequestData.shouldAbandon(maid)) {
            FoodRequestHandler.forceAbandon(maid);
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "已将索要年龄设为 " + days + " 游戏日，已达到放弃阈值并立即放弃"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "已将索要年龄设为 " + days + " 游戏日（abandonIn="
                            + FoodRequestData.daysUntilAbandon(maid) + "）"), false);
        }
        return 1;
    }

    private static int requestAbandon(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        if (!FoodRequestHandler.forceAbandon(maid)) {
            ctx.getSource().sendFailure(Component.literal("当前没有进行中的索要"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("已强制触发放弃索要"), false);
        return 1;
    }

    private static int setRequestIntervalOverride(CommandContext<CommandSourceStack> ctx) {
        int min = IntegerArgumentType.getInteger(ctx, "min");
        int max = IntegerArgumentType.getInteger(ctx, "max");
        DebugOverrides.setRequestIntervalDays(min, max);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "调试覆盖：索要间隔天数 = " + min + "-" + max + "（重启失效，不影响配置文件）"), true);
        return 1;
    }

    private static int clearRequestIntervalOverride(CommandContext<CommandSourceStack> ctx) {
        DebugOverrides.clearRequestIntervalDays();
        ctx.getSource().sendSuccess(() -> Component.literal("已清除索要间隔天数调试覆盖"), true);
        return 1;
    }

    private static int nagNow(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        ItemStack want = FoodRequestData.getRequestStack(maid);
        if (want.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("当前没有进行中的索要，可先 /eatproperly request now"));
            return 0;
        }
        String text = MaidSpeech.formatRequest(want, maid.getRandom());
        if (!text.isEmpty()) {
            MaidSpeech.speakToOwner(maid, text);
        }
        FoodRequestData.setLastNagGameTime(maid, maid.level().getGameTime());
        FoodRequestData.setNextNagIntervalTicks(maid,
                FoodRequestData.randomNagIntervalTicks(maid.getRandom()));
        ctx.getSource().sendSuccess(() -> Component.literal("已立即催促索要"), false);
        return 1;
    }

    private static int setNagIntervalOverride(CommandContext<CommandSourceStack> ctx) {
        int min = IntegerArgumentType.getInteger(ctx, "min");
        int max = IntegerArgumentType.getInteger(ctx, "max");
        DebugOverrides.setNagIntervalTicks(min, max);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "调试覆盖：催促间隔 ticks = " + min + "-" + max), true);
        return 1;
    }

    private static int clearNagIntervalOverride(CommandContext<CommandSourceStack> ctx) {
        DebugOverrides.clearNagIntervalTicks();
        ctx.getSource().sendSuccess(() -> Component.literal("已清除催促间隔调试覆盖"), true);
        return 1;
    }

    private static int satietySet(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        int value = IntegerArgumentType.getInteger(ctx, "value");
        SatietyData.ensureInitialized(maid);
        SatietyData.setSatiety(maid, value);
        if (value >= EatProperlyConfig.SATIETY_LOW_THRESHOLD.get()) {
            SatietyData.setLowSatietyDays(maid, 0);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("饱食度设为 " + SatietyData.getSatiety(maid)), true);
        return 1;
    }

    private static int satietyDrain(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        SatietyData.ensureInitialized(maid);
        int favorBefore = maid.getFavorabilityManager().getLevel();
        int drain = SatietyData.dailyDrainAmount();
        SatietyData.setSatiety(maid, SatietyData.getSatiety(maid) - drain);
        SatietyData.setLastSatietyDay(maid, SatietyData.currentGameDay(maid));
        int favorAfter = maid.getFavorabilityManager().getLevel();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "模拟日降 -" + drain + " → " + SatietyData.getSatiety(maid)
                        + "（好感等级 " + favorBefore + "→" + favorAfter + "，本指令不改好感）"), true);
        return 1;
    }

    private static int lowDaysSet(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        int days = IntegerArgumentType.getInteger(ctx, "days");
        SatietyData.ensureInitialized(maid);
        SatietyData.setLowSatietyDays(maid, days);
        ctx.getSource().sendSuccess(() -> Component.literal("低饱食连续天数设为 " + days), true);
        return 1;
    }

    private static int favorSetLevel(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        int level = IntegerArgumentType.getInteger(ctx, "level");
        level = Math.min(level, FavorabilityExtension.getMaxLevel());
        FavorabilityManager manager = maid.getFavorabilityManager();
        int oldLevel = manager.getLevel();
        int points = FavorabilityExtension.getPointByLevel(level);
        maid.setFavorability(points);
        int newLevel = manager.getLevel();
        if (oldLevel != newLevel) {
            manager.onFavorabilityLevelChange(oldLevel, newLevel);
        }
        int finalLevel = newLevel;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "好感设为等级 " + finalLevel + " (points=" + points + ", hp=" + maid.getMaxHealth() + ")"), true);
        return 1;
    }

    private static int clearAllOverrides(CommandContext<CommandSourceStack> ctx) {
        DebugOverrides.clearAll();
        ctx.getSource().sendSuccess(() -> Component.literal("已清除全部调试覆盖"), true);
        return 1;
    }

    private static int overrideStatus(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(
                "调试覆盖: requestDays=" + formatRange(DebugOverrides.requestDaysMin(), DebugOverrides.requestDaysMax())
                        + ", nagTicks=" + formatRange(DebugOverrides.nagTicksMin(), DebugOverrides.nagTicksMax())), false);
        return 1;
    }
}
