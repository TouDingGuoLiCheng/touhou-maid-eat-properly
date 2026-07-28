package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.advancement.EatProperlyAdvancements;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.favorability.FavorLevelMath;
import com.touhoumaid.eatproperly.favorability.FavorabilityExtension;
import com.touhoumaid.eatproperly.item.reward.DualLunchboxLogic;
import com.touhoumaid.eatproperly.milestone.MilestoneRewards;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemStackHandler;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class FoodRequestHandler {
    private FoodRequestHandler() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof EntityMaid maid)) {
            return;
        }
        if (!maid.isAlive() || !maid.isTame()) {
            return;
        }
        if (maid.tickCount % 20 != 0) {
            return;
        }
        tickMaid(maid);
    }

    private static void tickMaid(EntityMaid maid) {
        MaidSpeech.tickPendingChat(maid);
        MilestoneRewards.tickBubbleNag(maid);
        ensureSchedule(maid);

        long day = FoodRequestData.currentGameDay(maid);
        if (!FoodRequestData.hasRequest(maid) && day >= FoodRequestData.getNextRequestDay(maid)) {
            startNewRequest(maid);
        }

        if (!FoodRequestData.hasRequest(maid)) {
            return;
        }

        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty()) {
            FoodRequestData.clearRequest(maid);
            scheduleNext(maid);
            return;
        }

        if (tryCompleteFromBackpack(maid, wanted)) {
            return;
        }

        if (tryAbandonRequest(maid, wanted)) {
            return;
        }

        maybeNag(maid, wanted);
    }

    /**
     * After {@link EatProperlyConfig#REQUEST_ABANDON_DAYS} game days without fulfillment,
     * the maid politely gives up, speaks a sad line, and schedules the next request.
     */
    private static boolean tryAbandonRequest(EntityMaid maid, ItemStack wanted) {
        if (!FoodRequestData.shouldAbandon(maid)) {
            return false;
        }

        String text = MaidSpeech.formatAbandon(wanted, maid.getRandom());
        FoodRequestData.clearRequest(maid);
        scheduleNext(maid);
        if (!text.isEmpty()) {
            MaidSpeech.speakToOwner(maid, text);
        }
        return true;
    }

    /** Force-abandon current request (debug / commands). */
    public static boolean forceAbandon(EntityMaid maid) {
        if (!FoodRequestData.hasRequest(maid)) {
            return false;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        String text = wanted.isEmpty() ? "" : MaidSpeech.formatAbandon(wanted, maid.getRandom());
        FoodRequestData.clearRequest(maid);
        scheduleNext(maid);
        if (!text.isEmpty()) {
            MaidSpeech.speakToOwner(maid, text);
        }
        return true;
    }

    private static void ensureSchedule(EntityMaid maid) {
        if (FoodRequestData.isInitialized(maid)) {
            return;
        }
        FoodRequestData.setInitialized(maid);
        scheduleNext(maid);
    }

    private static void scheduleNext(EntityMaid maid) {
        long day = FoodRequestData.currentGameDay(maid);
        // At least 1 day gap prevents same-tick re-request → backpack auto-complete → favor loops
        // while testing with interval overrides of 0.
        int interval = Math.max(1, FoodRequestData.randomIntervalDays(maid.getRandom()));
        FoodRequestData.setNextRequestDay(maid, day + interval);
    }

    private static void startNewRequest(EntityMaid maid) {
        ItemStack pick = FoodPool.pickRandom(maid.getRandom());
        if (pick.isEmpty()) {
            scheduleNext(maid);
            return;
        }
        FoodRequestData.setRequest(maid, pick);
        nag(maid, pick);
    }

    /**
     * Completes the request when the wanted food is already in the maid inventory.
     * Does <b>not</b> remove the food — she should eat it herself (meal AI / edible blocks)
     * so satiety can restore normally.
     */
    private static boolean tryCompleteFromBackpack(EntityMaid maid, ItemStack wanted) {
        ItemStackHandler inv = maid.getMaidInv();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (isAcceptableFood(maid, stack, wanted)) {
                completeRequest(maid, wanted);
                return true;
            }
        }
        return false;
    }

    /**
     * Completes the current food request if {@code food} matches what the maid wants.
     * Does not consume or move the stack — caller keeps ownership / delivery separate.
     */
    public static boolean tryCompleteWithFood(EntityMaid maid, ItemStack food) {
        if (food.isEmpty() || !FoodRequestData.hasRequest(maid)) {
            return false;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty() || !isAcceptableFood(maid, food, wanted)) {
            return false;
        }
        completeRequest(maid, wanted);
        return true;
    }

    /** Match by item id so NBT/count differences (FD leftovers, etc.) still count. */
    public static boolean matchesRequest(ItemStack food, ItemStack wanted) {
        if (food.isEmpty() || wanted.isEmpty()) {
            return false;
        }
        String a = FoodPool.toId(food);
        String b = FoodPool.toId(wanted);
        return !a.isEmpty() && a.equals(b);
    }

    /**
     * Whether {@code food} completes the current request: an exact match normally, or any
     * food-pool item while the maid is under the hypnosis note's "forgotten" state.
     */
    public static boolean isAcceptableFood(EntityMaid maid, ItemStack food, ItemStack wanted) {
        if (food.isEmpty()) {
            return false;
        }
        if (FoodRequestData.isForgotten(maid)) {
            return FoodPool.isPoolItem(food);
        }
        return matchesRequest(food, wanted);
    }

    /**
     * Immediately (re)rolls the maid's request, used by the extra meal permit. Abandons any
     * in-progress request (silently, no sad line) before picking a new one.
     */
    public static boolean forceNewRequest(EntityMaid maid) {
        FoodRequestData.setInitialized(maid);
        FoodRequestData.clearRequest(maid);
        ItemStack pick = FoodPool.pickRandom(maid.getRandom());
        if (pick.isEmpty()) {
            scheduleNext(maid);
            return false;
        }
        FoodRequestData.setRequest(maid, pick);
        nag(maid, pick);
        return true;
    }

    /**
     * Whether two lunchbox-slot foods together satisfy the dual lunchbox's completion rule:
     * both must be valid food-pool items, and at least one must match (or, while forgotten,
     * simply be) the current request.
     */
    public static boolean canCompleteDual(EntityMaid maid, ItemStack foodA, ItemStack foodB) {
        if (!FoodRequestData.hasRequest(maid)) {
            return false;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty()) {
            return false;
        }
        boolean aMatches = isAcceptableFood(maid, foodA, wanted);
        boolean bMatches = isAcceptableFood(maid, foodB, wanted);
        boolean aInPool = FoodPool.isPoolItem(foodA);
        boolean bInPool = FoodPool.isPoolItem(foodB);
        return DualLunchboxLogic.canComplete(aMatches, aInPool, bMatches, bInPool);
    }

    /**
     * Completes the current request granting {@code levelGain} favorability levels instead of the
     * usual one (used by the dual lunchbox). No-op (returns false) if there is no active request.
     */
    public static boolean completeCurrentRequestWithLevelGain(EntityMaid maid, int levelGain) {
        if (!FoodRequestData.hasRequest(maid)) {
            return false;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty()) {
            return false;
        }
        completeRequest(maid, wanted, levelGain);
        return true;
    }

    /**
     * If the maid's backpack already holds the requested food, complete the request
     * without removing the item (she will eat it later).
     */
    public static boolean tryCompleteFromBackpackNow(EntityMaid maid) {
        if (!FoodRequestData.hasRequest(maid)) {
            return false;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty()) {
            return false;
        }
        return tryCompleteFromBackpack(maid, wanted);
    }

    private static void completeRequest(EntityMaid maid, ItemStack food) {
        completeRequest(maid, food, 1);
    }

    /** @param levelGain how many favorability levels this completion should grant (normally 1). */
    private static void completeRequest(EntityMaid maid, ItemStack food, int levelGain) {
        FoodRequestData.clearRequest(maid);
        scheduleNext(maid);

        FavorabilityManager manager = maid.getFavorabilityManager();
        int add = FavorLevelMath.pointsToAdd(maid.getFavorability(), levelGain, FavorabilityExtension.getMaxLevel(),
                FavorabilityExtension::getLevel, FavorabilityExtension::getPointByLevel);
        if (add > 0) {
            manager.add(add);
        }

        Player owner = maid.getOwner() instanceof Player player ? player : null;
        if (owner instanceof ServerPlayer serverPlayer) {
            EatProperlyAdvancements.onRequestCompleted(serverPlayer);
        }

        String thanks = MaidSpeech.formatThanks(food, maid.getRandom());
        if (!thanks.isEmpty()) {
            MaidSpeech.speakToOwner(maid, thanks);
        }
    }

    private static void maybeNag(EntityMaid maid, ItemStack wanted) {
        long now = maid.level().getGameTime();
        long last = FoodRequestData.getLastNagGameTime(maid);
        int interval = FoodRequestData.getNextNagIntervalTicks(maid);
        if (interval <= 0) {
            interval = FoodRequestData.randomNagIntervalTicks(maid.getRandom());
            FoodRequestData.setNextNagIntervalTicks(maid, interval);
        }
        if (last >= 0 && now - last < interval) {
            return;
        }
        nag(maid, wanted);
    }

    private static void nag(EntityMaid maid, ItemStack wanted) {
        String text = MaidSpeech.formatRequest(wanted, maid.getRandom());
        if (text.isEmpty()) {
            return;
        }
        MaidSpeech.speakToOwner(maid, text);
        FoodRequestData.setLastNagGameTime(maid, maid.level().getGameTime());
        FoodRequestData.setNextNagIntervalTicks(maid, FoodRequestData.randomNagIntervalTicks(maid.getRandom()));
    }
}
