package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.advancement.EatProperlyAdvancements;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.favorability.FavorabilityExtension;
import com.touhoumaid.eatproperly.favorability.RequestFavorPolicy;
import com.touhoumaid.eatproperly.item.reward.DualLunchboxLogic;
import com.touhoumaid.eatproperly.milestone.MilestoneRewards;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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
        MaidLoadedClock.sync(maid);
        MaidSpeech.tickPendingChat(maid);
        MilestoneRewards.tickBubbleNag(maid);
        ensureSchedule(maid);
        resolveScheduleCollisionIfNeeded(maid);

        long day = FoodRequestData.currentGameDay(maid);
        if (!FoodRequestData.hasRequest(maid) && day >= FoodRequestData.getNextRequestDay(maid)) {
            startNewRequest(maid);
        }

        if (!FoodRequestData.hasRequest(maid)) {
            return;
        }

        if (maybeRematchForOrderBook(maid)) {
            return;
        }

        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty()) {
            FoodRequestData.clearRequest(maid);
            scheduleNext(maid);
            return;
        }

        clearLegacyForgottenFlag(maid);

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
            MaidSpeech.bubbleToOwnerIfNear(maid, text);
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
            MaidSpeech.bubbleToOwnerIfNear(maid, text);
        }
        return true;
    }

    /**
     * Clears the maid's current food request without completion, favor gain, or thank-you speech.
     * Used by the hypnosis note; schedules the next natural request window.
     */
    public static boolean forgetCurrentRequest(EntityMaid maid) {
        if (!FoodRequestData.hasRequest(maid)) {
            return false;
        }
        FoodRequestData.clearRequest(maid);
        scheduleNext(maid);
        return true;
    }

    private static void ensureSchedule(EntityMaid maid) {
        if (FoodRequestData.isInitialized(maid)) {
            return;
        }
        FoodRequestData.setInitialized(maid);
        scheduleNext(maid);
    }

    /**
     * If another same-owner loaded maid shares this maid's untriggered {@code NextRequestDay},
     * the maid with the larger UUID yields and reschedules (stable, no mutual kick).
     */
    private static void resolveScheduleCollisionIfNeeded(EntityMaid maid) {
        if (FoodRequestData.hasRequest(maid)) {
            return;
        }
        long myDay = FoodRequestData.getNextRequestDay(maid);
        UUID owner = maid.getOwnerUUID();
        if (owner == null || !(maid.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        UUID myUuid = maid.getUUID();
        for (Entity entity : serverLevel.getAllEntities()) {
            if (!(entity instanceof EntityMaid other) || other == maid || !other.isAlive() || !other.isTame()) {
                continue;
            }
            UUID otherOwner = other.getOwnerUUID();
            if (otherOwner == null || !owner.equals(otherOwner)) {
                continue;
            }
            if (FoodRequestData.hasRequest(other)) {
                continue;
            }
            if (FoodRequestData.getNextRequestDay(other) != myDay) {
                continue;
            }
            // Larger UUID yields so only one maid moves per conflicting pair.
            if (myUuid.compareTo(other.getUUID()) > 0) {
                scheduleNext(maid);
                return;
            }
        }
    }

    /** Schedules the next natural request window with same-owner day deconfliction. */
    public static void scheduleNext(EntityMaid maid) {
        long today = FoodRequestData.currentGameDay(maid);
        int min = FoodRequestData.requestIntervalDaysMin();
        int max = FoodRequestData.requestIntervalDaysMax();
        int last = FoodRequestData.getLastIntervalDays(maid);
        Set<Long> occupied = collectOccupiedNextDays(maid);
        Random random = new Random(maid.getRandom().nextLong());
        long[] picked = FoodRequestSchedule.pickNextRequestDay(today, min, max, last, occupied, random);
        FoodRequestData.setNextRequestDay(maid, picked[0]);
        FoodRequestData.setLastIntervalDays(maid, (int) picked[1]);
    }

    /** Absolute next-request days already claimed by other same-owner loaded maids (waiting only). */
    private static Set<Long> collectOccupiedNextDays(EntityMaid maid) {
        Set<Long> occupied = new HashSet<>();
        UUID owner = maid.getOwnerUUID();
        if (owner == null || !(maid.level() instanceof ServerLevel serverLevel)) {
            return occupied;
        }
        for (Entity entity : serverLevel.getAllEntities()) {
            if (!(entity instanceof EntityMaid other) || other == maid || !other.isAlive() || !other.isTame()) {
                continue;
            }
            UUID otherOwner = other.getOwnerUUID();
            if (otherOwner == null || !owner.equals(otherOwner)) {
                continue;
            }
            if (FoodRequestData.hasRequest(other)) {
                continue;
            }
            occupied.add(FoodRequestData.getNextRequestDay(other));
        }
        return occupied;
    }

    private static void startNewRequest(EntityMaid maid) {
        List<OrderBookSnapshot> books = OrderBookData.findAllEquipped(maid);
        ItemStack pick = FoodPool.pickRandom(maid.getRandom(), books);
        if (pick.isEmpty()) {
            scheduleNext(maid);
            if (!books.isEmpty()) {
                String text = MaidSpeech.formatEmptyMenu(maid.getRandom());
                if (!text.isEmpty()) {
                    MaidSpeech.bubbleToOwnerIfNear(maid, text);
                }
            }
            return;
        }
        FoodRequestData.setRequest(maid, pick);
        nag(maid, pick);
    }

    /**
     * When Order Book(s) are equipped and the current request is no longer in the
     * intersection of their filters, re-roll immediately (or clear + empty-menu speech).
     *
     * @return true when the tick should stop further request handling this pass
     */
    private static boolean maybeRematchForOrderBook(EntityMaid maid) {
        List<OrderBookSnapshot> books = OrderBookData.findAllEquipped(maid);
        if (books.isEmpty()) {
            return false;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty()) {
            return false;
        }
        String wantedId = FoodPool.toId(wanted);
        if (wantedId.isEmpty() || OrderBookIntersection.allowsAll(books, wantedId)) {
            return false;
        }
        ItemStack pick = FoodPool.pickRandom(maid.getRandom(), books);
        if (pick.isEmpty()) {
            FoodRequestData.clearRequest(maid);
            scheduleNext(maid);
            String text = MaidSpeech.formatEmptyMenu(maid.getRandom());
            if (!text.isEmpty()) {
                MaidSpeech.bubbleToOwnerIfNear(maid, text);
            }
            return true;
        }
        FoodRequestData.setRequest(maid, pick);
        String text = MaidSpeech.formatOrderBookRematch(pick, maid.getRandom());
        if (!text.isEmpty()) {
            MaidSpeech.bubbleToOwnerIfNear(maid, text);
        }
        return true;
    }

    /**
     * Legacy {@code RequestForgotten} from old hypnosis builds widened completion to any pool item.
     * Hypnosis now clears the request outright; drop the stale flag so GUI and matching stay strict.
     */
    private static void clearLegacyForgottenFlag(EntityMaid maid) {
        if (FoodRequestData.isForgotten(maid)) {
            FoodRequestData.setForgotten(maid, false);
        }
    }

    /**
     * Completes the request when the wanted food is already in the maid's available backpack or hands.
     * Does <b>not</b> remove the food — she should eat it herself (meal AI / edible blocks)
     * so satiety can restore normally. Called from delivery paths (GUI close, lunchbox, give, picnic).
     * Not from the periodic tick.
     */
    private static boolean tryCompleteFromCarried(EntityMaid maid, ItemStack wanted, boolean speakThanks) {
        if (tryCompleteFromItemHandler(maid, maid.getAvailableBackpackInv(), wanted, speakThanks)) {
            return true;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            if (matchesRequest(maid.getItemInHand(hand), wanted)) {
                completeRequest(maid, wanted, RequestFavorPolicy.NORMAL_POINTS, speakThanks);
                return true;
            }
        }
        return false;
    }

    /**
     * Completes when {@code handler} (backpack / picnic basket / picnic mat) holds the wanted food.
     * Does not remove the food.
     */
    public static boolean tryCompleteFromItemHandler(EntityMaid maid, IItemHandler handler) {
        if (!FoodRequestData.hasRequest(maid) || handler == null) {
            return false;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty()) {
            return false;
        }
        return tryCompleteFromItemHandler(maid, handler, wanted, true);
    }

    private static boolean tryCompleteFromItemHandler(EntityMaid maid, IItemHandler handler, ItemStack wanted) {
        return tryCompleteFromItemHandler(maid, handler, wanted, true);
    }

    private static boolean tryCompleteFromItemHandler(
            EntityMaid maid, IItemHandler handler, ItemStack wanted, boolean speakThanks) {
        String wantedId = FoodPool.toId(wanted);
        String[] slotIds = new String[handler.getSlots()];
        for (int i = 0; i < handler.getSlots(); i++) {
            slotIds[i] = FoodPool.toId(handler.getStackInSlot(i));
        }
        if (!FoodHandlerMatch.containsWanted(slotIds, wantedId)) {
            return false;
        }
        completeRequest(maid, wanted, RequestFavorPolicy.NORMAL_POINTS, speakThanks);
        return true;
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

    /** Whether {@code food} is the item the maid currently wants (same item id). */
    public static boolean isAcceptableFood(EntityMaid maid, ItemStack food, ItemStack wanted) {
        if (food.isEmpty()) {
            return false;
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
        List<OrderBookSnapshot> books = OrderBookData.findAllEquipped(maid);
        ItemStack pick = FoodPool.pickRandom(maid.getRandom(), books);
        if (pick.isEmpty()) {
            scheduleNext(maid);
            if (!books.isEmpty()) {
                String text = MaidSpeech.formatEmptyMenu(maid.getRandom());
                if (!text.isEmpty()) {
                    MaidSpeech.bubbleToOwnerIfNear(maid, text);
                }
            }
            return false;
        }
        FoodRequestData.setRequest(maid, pick);
        nagImmediate(maid, pick);
        return true;
    }

    /**
     * Whether two lunchbox-slot foods together satisfy the dual lunchbox's completion rule:
     * both must be valid food-pool items, and at least one must match the current request.
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
     * Completes the current request granting a flat favorability point reward
     * (used by the dual lunchbox for {@link RequestFavorPolicy#DUAL_POINTS}).
     * No-op (returns false) if there is no active request.
     */
    public static boolean completeCurrentRequestWithPoints(EntityMaid maid, int points) {
        if (!FoodRequestData.hasRequest(maid)) {
            return false;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty()) {
            return false;
        }
        completeRequest(maid, wanted, points, true);
        return true;
    }

    /**
     * If the maid's backpack or hands already hold the requested food, complete the request
     * without removing the item (she will eat it later). Speaks the normal request-thanks line.
     */
    public static boolean tryCompleteFromCarriedNow(EntityMaid maid) {
        return tryCompleteFromCarriedNow(maid, true);
    }

    /**
     * @param speakThanks when false, skip the normal request-thanks (caller may use another line)
     */
    public static boolean tryCompleteFromCarriedNow(EntityMaid maid, boolean speakThanks) {
        if (!FoodRequestData.hasRequest(maid)) {
            return false;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty()) {
            return false;
        }
        return tryCompleteFromCarried(maid, wanted, speakThanks);
    }

    private static void completeRequest(EntityMaid maid, ItemStack food) {
        completeRequest(maid, food, RequestFavorPolicy.NORMAL_POINTS, true);
    }

    /** @param points flat favorability points to grant (clamped to the favorability cap). */
    private static void completeRequest(EntityMaid maid, ItemStack food, int points, boolean speakThanks) {
        FoodRequestData.clearRequest(maid);
        scheduleNext(maid);

        FavorabilityManager manager = maid.getFavorabilityManager();
        int add = RequestFavorPolicy.clampedGrant(
                maid.getFavorability(), points, FavorabilityExtension.getMaxPoint());
        if (add > 0) {
            manager.add(add);
        }

        Player owner = maid.getOwner() instanceof Player player ? player : null;
        if (owner instanceof ServerPlayer serverPlayer) {
            EatProperlyAdvancements.onRequestCompleted(serverPlayer);
        }

        if (!speakThanks) {
            return;
        }
        String thanks = MaidSpeech.formatThanks(food, maid.getRandom());
        if (!thanks.isEmpty()) {
            MaidSpeech.speakThanksToOwner(maid, thanks);
        }
    }

    private static void maybeNag(EntityMaid maid, ItemStack wanted) {
        if (MaidLoadedClock.getNagElapsed(maid) < MaidSpeech.REQUEST_NAG_COOLDOWN_TICKS) {
            return;
        }
        nag(maid, wanted);
    }

    private static void nag(EntityMaid maid, ItemStack wanted) {
        String text = MaidSpeech.formatRequest(wanted, maid.getRandom());
        if (text.isEmpty()) {
            return;
        }
        if (!MaidSpeech.bubbleToOwnerIfNear(maid, text)) {
            return;
        }
        MaidLoadedClock.resetNagElapsed(maid);
        FoodRequestData.setLastNagGameTime(maid, maid.level().getGameTime());
        FoodRequestData.setNextNagIntervalTicks(maid, MaidSpeech.REQUEST_NAG_COOLDOWN_TICKS);
    }

    /** Extra-meal-permit: same bubble-near nag (no public chat). */
    private static void nagImmediate(EntityMaid maid, ItemStack wanted) {
        nag(maid, wanted);
    }
}
