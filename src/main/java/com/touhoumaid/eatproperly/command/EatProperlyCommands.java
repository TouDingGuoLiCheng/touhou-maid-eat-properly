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
import com.touhoumaid.eatproperly.food.MaidLoadedClock;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import com.touhoumaid.eatproperly.inventory.TrinketContainer;
import com.touhoumaid.eatproperly.milestone.MilestoneData;
import com.touhoumaid.eatproperly.milestone.MilestoneRewards;
import com.touhoumaid.eatproperly.satiety.HealMealData;
import com.touhoumaid.eatproperly.satiety.HealMealPolicy;
import com.touhoumaid.eatproperly.satiety.HungerBackpackMeal;
import com.touhoumaid.eatproperly.satiety.MaidMealHungerPolicy;
import com.touhoumaid.eatproperly.satiety.MealFavorCooldown;
import com.touhoumaid.eatproperly.satiety.MaidFoodData;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import com.touhoumaid.eatproperly.snackraid.SnackRaidData;
import com.touhoumaid.eatproperly.snackraid.SnackRaidFood;
import com.touhoumaid.eatproperly.snackraid.SnackRaidHandler;
import com.touhoumaid.eatproperly.snackraid.SnackRaidPathing;
import com.touhoumaid.eatproperly.snackraid.SnackRaidPhase;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
        // Root itself is unrestricted so the "trinkets" branch below stays usable by any player;
        // every debug/admin branch below carries its own .requires(permission 2) instead.
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("eatproperly")
                .then(Commands.literal("status")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> status(ctx, null))
                        .then(Commands.argument("maid", EntityArgument.entity())
                                .executes(ctx -> status(ctx, EntityArgument.getEntity(ctx, "maid")))))
                .then(Commands.literal("request")
                        .requires(source -> source.hasPermission(2))
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
                        .requires(source -> source.hasPermission(2))
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
                        .requires(source -> source.hasPermission(2))
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
                .then(Commands.literal("hunger")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("now")
                                .executes(ctx -> hungerNow(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> hungerNow(ctx, EntityArgument.getEntity(ctx, "maid")))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0, 20))
                                        .executes(ctx -> hungerSet(ctx)))))
                .then(Commands.literal("favor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("setlevel")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 1000))
                                        .executes(ctx -> favorSetLevel(ctx, null))
                                        .then(Commands.argument("maid", EntityArgument.entity())
                                                .executes(ctx -> favorSetLevel(ctx, EntityArgument.getEntity(ctx, "maid")))))))
                .then(Commands.literal("snackraid")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reset")
                                .executes(ctx -> snackRaidReset(ctx, null))
                                .then(Commands.literal("all")
                                        .executes(EatProperlyCommands::snackRaidResetAll))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> snackRaidReset(ctx, EntityArgument.getEntity(ctx, "maid")))))
                        .then(Commands.literal("cancel")
                                .executes(ctx -> snackRaidCancel(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> snackRaidCancel(ctx, EntityArgument.getEntity(ctx, "maid")))))
                        .then(Commands.literal("status")
                                .executes(ctx -> snackRaidStatus(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> snackRaidStatus(ctx, EntityArgument.getEntity(ctx, "maid"))))))
                .then(Commands.literal("meal")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> mealStatus(ctx, null))
                        .then(Commands.literal("status")
                                .executes(ctx -> mealStatus(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> mealStatus(ctx, EntityArgument.getEntity(ctx, "maid")))))
                        .then(Commands.literal("clearcd")
                                .executes(ctx -> mealClearCd(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> mealClearCd(ctx, EntityArgument.getEntity(ctx, "maid")))))
                        .then(Commands.argument("maid", EntityArgument.entity())
                                .executes(ctx -> mealStatus(ctx, EntityArgument.getEntity(ctx, "maid")))))
                .then(Commands.literal("trinkets")
                        // Intentionally no .requires(): default open method for any player (spec: §2.3 / task-9-brief).
                        .executes(EatProperlyCommands::openTrinkets))
                .then(Commands.literal("milestone")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("pending")
                                .executes(ctx -> milestonePending(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> milestonePending(ctx, EntityArgument.getEntity(ctx, "maid")))))
                        .then(Commands.literal("claim")
                                .executes(ctx -> milestoneClaim(ctx, null))
                                .then(Commands.argument("maid", EntityArgument.entity())
                                        .executes(ctx -> milestoneClaim(ctx, EntityArgument.getEntity(ctx, "maid"))))))
                .then(Commands.literal("override")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("clear")
                                .executes(EatProperlyCommands::clearAllOverrides))
                        .then(Commands.literal("status")
                                .executes(EatProperlyCommands::overrideStatus)));

        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(root);
        dispatcher.register(Commands.literal("ep")
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
        String mealBlock = formatMealGateBlock(maid);
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                "[EatProperly] %s\n" +
                        "  day=%d nextRequestDay=%d request=%s startDay=%s abandonIn=%s abandonDays=%d\n" +
                        "  satiety=%d/%d saturation=%.1f exhaustion=%.1f lowDays=%d lastSatietyDay=%d\n" +
                        "  favorLv=%d points=%d nextNeed=%d hp=%.1f\n" +
                        "  cfgRequestDays=%d-%d cfgNagTicks=%d-%d\n" +
                        "  overrideRequestDays=%s overrideNagTicks=%s\n" +
                        "%s",
                maid.getDisplayName().getString(),
                day,
                FoodRequestData.getNextRequestDay(maid),
                wantName,
                startDay < 0 ? "-" : Long.toString(startDay),
                abandonIn < 0 ? "-" : Long.toString(abandonIn),
                EatProperlyConfig.REQUEST_ABANDON_DAYS.get(),
                SatietyData.getSatiety(maid),
                EatProperlyConfig.SATIETY_MAX,
                MaidFoodData.getSaturation(maid),
                MaidFoodData.getExhaustion(maid),
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
                formatRange(DebugOverrides.nagTicksMin(), DebugOverrides.nagTicksMax()),
                mealBlock
        )), false);
        return 1;
    }

    private static int mealStatus(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        SatietyData.ensureInitialized(maid);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "[EatProperly] meal " + maid.getDisplayName().getString() + "\n" + formatMealGateBlock(maid)), false);
        return 1;
    }

    private static int mealClearCd(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        FavorabilityManager fav = maid.getFavorabilityManager();
        String before = formatMealCdLine(fav);
        MealFavorCooldown.clearMealCooldowns(fav);
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                "[EatProperly] 已清除 %s 的 WorkMeal/HomeMeal 冷却\n  清除前：%s\n  清除后：%s",
                maid.getDisplayName().getString(), before, formatMealCdLine(fav))), true);
        return 1;
    }

    private static String formatMealGateBlock(EntityMaid maid) {
        SatietyData.ensureInitialized(maid);
        int satiety = SatietyData.getSatiety(maid);
        int gate = EatProperlyConfig.EAT_WHEN_SATIETY_BELOW.get();
        int bingeUntil = EatProperlyConfig.HUNGRY_BINGE_UNTIL_SATIETY.get();
        int low = EatProperlyConfig.SATIETY_LOW_THRESHOLD.get();
        MaidMealHungerPolicy.MealZone zone = MaidMealHungerPolicy.mealZone(satiety, bingeUntil, gate);
        FavorabilityManager fav = maid.getFavorabilityManager();
        boolean workFood = HungerBackpackMeal.hasEdibleFood(maid);
        boolean mainEmpty = maid.getMainHandItem().isEmpty();
        boolean offEmpty = maid.getOffhandItem().isEmpty();
        long now = maid.level().getGameTime();
        int healCd = EatProperlyConfig.HEAL_MEAL_COOLDOWN_TICKS.get();
        long healRemain = HealMealPolicy.remainingCooldownTicks(
                HealMealData.getLastHealMealTime(maid), now, healCd);
        boolean healAllowed = HealMealPolicy.allowsEat(
                satiety, gate, HealMealData.getLastHealMealTime(maid), now, healCd);
        String hint = switch (zone) {
            case BINGE -> "低饱食连吃中：约每 2.5s 可再开餐；若 workFood=false 会空转重试";
            case NORMAL_CD -> "正常冷却区：真吃但受 TLM 餐冷却；可用 /ep meal clearcd 清冷却";
            case FAKE_SKIP -> "过饱假跳过：不真吃、只刷餐冷却";
        };
        return String.format(
                "  meal: satiety=%d saturation=%.1f exhaustion=%.1f gate=%d bingeUntil=%d lowThreshold=%d zone=%s\n" +
                        "  %s\n" +
                        "  workFood=%s hands(mainEmpty=%s offEmpty=%s)\n" +
                        "  %s\n" +
                        "  healSelf allowed=%s remain=%d\n" +
                        "  hint: %s",
                satiety, MaidFoodData.getSaturation(maid), MaidFoodData.getExhaustion(maid),
                gate, bingeUntil, low, zone.name(),
                formatMealTypeLine(fav, MaidMealHungerPolicy.WORK_MEAL_TYPE),
                workFood, mainEmpty, offEmpty,
                formatMealTypeLine(fav, MaidMealHungerPolicy.HOME_MEAL_TYPE),
                healAllowed, healRemain,
                hint);
    }

    private static String formatMealCdLine(FavorabilityManager fav) {
        return formatMealTypeLine(fav, MaidMealHungerPolicy.WORK_MEAL_TYPE) + " | "
                + formatMealTypeLine(fav, MaidMealHungerPolicy.HOME_MEAL_TYPE);
    }

    private static String formatMealTypeLine(FavorabilityManager fav, String type) {
        int remain = MealFavorCooldown.remainingTicks(fav, type);
        boolean raw = MealFavorCooldown.rawCanAdd(fav, type);
        boolean effective = fav.canAdd(type);
        return String.format("%s remain=%d rawCanAdd=%s canAdd=%s", type, remain, raw, effective);
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
            MaidSpeech.bubbleToOwnerIfNear(maid, text);
        }
        // Mark nag as just done so the next tick's maybeNag does not repeat the line.
        MaidLoadedClock.resetNagElapsed(maid);
        FoodRequestData.setLastNagGameTime(maid, maid.level().getGameTime());
        FoodRequestData.setNextNagIntervalTicks(maid, MaidSpeech.REQUEST_NAG_COOLDOWN_TICKS);
        ItemStack finalPick = pick;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "已立即索要: " + finalPick.getHoverName().getString()), false);
        return 1;
    }

    private static int requestClear(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        FoodRequestData.clearRequest(maid);
        long day = FoodRequestData.currentGameDay(maid);
        FoodRequestHandler.scheduleNext(maid);
        long next = FoodRequestData.getNextRequestDay(maid);
        long interval = next - day;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "已清除索要，下次约 " + interval + " 日后 (day=" + next + ")"), true);
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
            MaidSpeech.bubbleToOwnerIfNear(maid, text);
        }
        MaidLoadedClock.resetNagElapsed(maid);
        FoodRequestData.setLastNagGameTime(maid, maid.level().getGameTime());
        FoodRequestData.setNextNagIntervalTicks(maid, MaidSpeech.REQUEST_NAG_COOLDOWN_TICKS);
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

    /**
     * Debug: force low satiety if needed, then play one hunger line (skips eat-first).
     * Does not change the player's food level — see {@link #hungerSet}.
     */
    private static int hungerNow(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        SatietyData.ensureInitialized(maid);
        int threshold = EatProperlyConfig.SATIETY_LOW_THRESHOLD.get();
        int before = SatietyData.getSatiety(maid);
        boolean lowered = false;
        if (before >= threshold) {
            int forced = Math.max(0, threshold - 1);
            SatietyData.setSatiety(maid, forced);
            lowered = true;
        }
        String text = MaidSpeech.formatHunger(maid, maid.getRandom());
        if (text.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("饥饿话术列表为空"));
            return 0;
        }
        boolean spoken = MaidSpeech.bubbleToOwnerIfNear(maid, text);
        if (spoken) {
            MaidLoadedClock.resetHungerElapsed(maid);
            SatietyData.setLastHungerSpeechGameTime(maid, maid.level().getGameTime());
        }
        int after = SatietyData.getSatiety(maid);
        boolean finalLowered = lowered;
        boolean finalSpoken = spoken;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "【女仆】已立即触发饥饿话术（跳过有粮先吃；非 /ep hunger set）"
                        + (finalLowered ? "；饱食 " + before + "→" + after : "；饱食已低=" + after)
                        + (finalSpoken ? "" : "；主人不在附近，未出气泡")), false);
        return 1;
    }

    /** Sets the executing player's foodLevel only; saturation is left unchanged. */
    private static int hungerSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int value = IntegerArgumentType.getInteger(ctx, "value");
        player.getFoodData().setFoodLevel(value);
        int actual = player.getFoodData().getFoodLevel();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "【玩家】饥饿度设为 " + actual + "/20（仅玩家，不改女仆饱食；女仆饥饿话术请用 /ep hunger now）"), true);
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

    /** Debug: drop the snack-raid cooldowns so the next scan may start a raid immediately. */
    private static int snackRaidReset(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        String before = describeSnackRaidCooldowns(maid);
        SnackRaidHandler.resetCooldown(maid);
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                "[EatProperly] 已重置 %s 的偷吃冷却（清除前：%s）\n"
                        + "  最多 %d tick 后的下次扫描即可重新触发；仍需满足低饱食、背包无食物、开关为允许",
                maid.getDisplayName().getString(), before,
                EatProperlyConfig.SNACK_RAID_SCAN_INTERVAL_TICKS.get())), true);
        return 1;
    }

    /** Debug: same as reset, for every maid nearby — avoids resetting the wrong one of several maids. */
    private static int snackRaidResetAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        List<EntityMaid> maids = player.level().getEntitiesOfClass(
                EntityMaid.class, player.getBoundingBox().inflate(32.0D));
        if (maids.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("32 格内没有女仆"));
            return 0;
        }
        StringBuilder sb = new StringBuilder("[EatProperly] 已重置 ")
                .append(maids.size()).append(" 只女仆的偷吃冷却：");
        for (EntityMaid maid : maids) {
            sb.append("\n  ").append(maid.getDisplayName().getString())
                    .append(" — 清除前 ").append(describeSnackRaidCooldowns(maid));
            SnackRaidHandler.resetCooldown(maid);
        }
        String message = sb.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(message), true);
        return maids.size();
    }

    /** Debug: stop an in-progress raid without arming the failure breather. */
    private static int snackRaidCancel(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        SnackRaidPhase phase = SnackRaidData.getPhase(maid);
        if (!SnackRaidHandler.cancelActive(maid)) {
            ctx.getSource().sendFailure(Component.literal(
                    maid.getDisplayName().getString() + " 当前没有进行中的偷吃"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                "[EatProperly] 已中止 %s 的偷吃（原阶段 %s），寻路已交还大脑",
                maid.getDisplayName().getString(), phase)), true);
        return 1;
    }

    /**
     * Debug: everything that decides whether a raid runs, including who currently owns the walk
     * target — that is how a hijacked path shows up.
     */
    private static int snackRaidStatus(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        SatietyData.ensureInitialized(maid);
        BlockPos chest = SnackRaidData.getChestPos(maid);
        BlockPos stand = SnackRaidData.getStandPos(maid);
        BlockPos cached = SnackRaidData.getCachedChestPos(maid);
        LivingEntity owner = maid.getOwner();
        String ownerInfo = owner == null
                ? "(无主人)"
                : String.format("%.1f 格（跟随停止距离 %.0f）", maid.distanceTo(owner), maid.getRestrictRadius() - 2.0F);

        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                "[EatProperly] 偷吃状态 %s\n"
                        + "  阶段=%s tick=%d 箱子=%s 站位=%s 缓存箱子=%s\n"
                        + "  当前 WALK_TARGET=%s（不是站位就说明被别的任务抢了）\n"
                        + "  冷却：%s\n"
                        + "  开关=%s 饱食=%d/触发阈值%d 手上/可用背包有效池食物=%s 可被大脑驱动=%s 主人距离=%s",
                maid.getDisplayName().getString(),
                SnackRaidData.getPhase(maid),
                SnackRaidData.getPhaseTicks(maid),
                chest.equals(BlockPos.ZERO) ? "(无)" : chest.toShortString(),
                stand.equals(BlockPos.ZERO) ? "(无)" : stand.toShortString(),
                cached.equals(BlockPos.ZERO) ? "(无)" : cached.toShortString(),
                SnackRaidPathing.describeWalkTarget(maid),
                describeSnackRaidCooldowns(maid),
                SnackRaidData.isAllowedLocally(maid) ? "允许" : "禁止",
                SatietyData.getSatiety(maid),
                EatProperlyConfig.SATIETY_LOW_THRESHOLD.get(),
                SnackRaidFood.hasBackpackFood(maid) ? "是" : "否",
                maid.canBrainMoving() ? "是" : "否",
                ownerInfo)), false);
        return 1;
    }

    private static String describeSnackRaidCooldowns(EntityMaid maid) {
        long now = maid.level().getGameTime();
        long lastRaid = SnackRaidData.getLastRaidTime(maid);
        long successLeft = lastRaid <= 0L
                ? 0L
                : Math.max(0L, EatProperlyConfig.SNACK_RAID_COOLDOWN_TICKS.get() - (now - lastRaid));
        long retryLeft = Math.max(0L, SnackRaidData.getRetryAfter(maid) - now);
        return String.format("成功冷却剩 %d tick，失败重试剩 %d tick", successLeft, retryLeft);
    }

    private static int openTrinkets(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TrinketContainer.open(player);
        return 1;
    }

    /** Debug: list pending milestone levels/items for the target maid (nearest by default). */
    private static int milestonePending(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        List<MilestoneData.PendingClaim> pending = MilestoneData.listPendingClaims(maid);
        String maidName = maid.getDisplayName().getString();
        if (pending.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "[EatProperly] " + maidName + " 当前没有待领取的里程碑"), false);
            return 0;
        }
        StringBuilder sb = new StringBuilder("[EatProperly] ").append(maidName).append(" 待领取：");
        for (MilestoneData.PendingClaim claim : pending) {
            sb.append("\n  level=").append(claim.level()).append(" item=").append(claim.itemId());
        }
        String message = sb.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(message), false);
        return pending.size();
    }

    /** Debug: claim the first pending milestone reward for the target maid, mirroring the GUI claim flow. */
    private static int milestoneClaim(CommandContext<CommandSourceStack> ctx, Entity explicit) throws CommandSyntaxException {
        EntityMaid maid = resolveMaid(ctx.getSource(), explicit);
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        List<MilestoneData.PendingClaim> pending = MilestoneData.listPendingClaims(maid);
        if (pending.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("当前没有待领取的里程碑"));
            return 0;
        }
        int level = pending.get(0).level();
        ItemStack granted = MilestoneRewards.claimFirst(maid, player, level);
        if (granted.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(
                    "领取失败（你不是该女仆的主人，或队首等级已变化，可重试）"));
            return 0;
        }
        ItemStack finalGranted = granted;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "已领取 level=" + level + "：" + finalGranted.getHoverName().getString()), true);
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
