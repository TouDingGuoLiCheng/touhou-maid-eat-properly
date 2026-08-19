package com.touhoumaid.eatproperly.snackraid;

import com.github.tartaricacid.touhoulittlemaid.item.ItemPicnicBasket;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.food.FoodPermitData;
import com.touhoumaid.eatproperly.food.FoodPermitFetchData;
import com.touhoumaid.eatproperly.food.FoodPermitFetchPolicy;
import com.touhoumaid.eatproperly.food.FoodPermitFetchPolicy.FetchPlan;
import com.touhoumaid.eatproperly.food.FoodPermitMark;
import com.touhoumaid.eatproperly.food.FoodPermitSpeechPolicy;
import com.touhoumaid.eatproperly.food.FoodPermitTargets;
import com.touhoumaid.eatproperly.food.FoodPermitTransfer;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import com.touhoumaid.eatproperly.food.PicnicRefillData;
import com.touhoumaid.eatproperly.food.PicnicRefillPolicy;
import com.touhoumaid.eatproperly.satiety.MaidMealHungerPolicy;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class SnackRaidHandler {
    /** Breather after a failed raid, so an unreachable chest is not retried every scan. */
    private static final int FAILED_RETRY_DELAY_TICKS = 200;

    private record FoodPermitStart(SnackRaidTarget target, FetchPlan plan) {
    }

    private SnackRaidHandler() {
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
        tryFlushPendingPicnicRefill(maid);
        if (SnackRaidData.isActive(maid)) {
            tickActive(maid);
            return;
        }
        if (!shouldScanThisTick(maid)) {
            return;
        }
        maybeStartRaid(maid);
    }

    /** Clears both cooldowns so the next scan may start a raid. Leaves a running raid alone. */
    public static void resetCooldown(EntityMaid maid) {
        SnackRaidData.clearCooldowns(maid);
    }

    /** Stops a running raid without arming the failure breather, handing movement back to the brain. */
    public static boolean cancelActive(EntityMaid maid) {
        if (!SnackRaidData.isActive(maid)) {
            return false;
        }
        SnackRaidPathing.releaseControl(maid);
        SnackRaidData.clearActive(maid);
        return true;
    }

    private static boolean shouldScanThisTick(EntityMaid maid) {
        int interval = EatProperlyConfig.SNACK_RAID_SCAN_INTERVAL_TICKS.get();
        long tick = maid.level().getGameTime() + maid.getId();
        return tick % interval == 0;
    }

    private static void maybeStartRaid(EntityMaid maid) {
        if (maid.isSleeping()) {
            return;
        }
        // Sitting, riding or leashed maids cannot be moved by the brain, so a raid could never finish.
        if (!maid.canBrainMoving()) {
            return;
        }
        boolean seekingPicnicRefill = PicnicRefillData.isSeeking(maid);
        // Failure breather must not strand empty-mat seeking (she already left the mat).
        if (!seekingPicnicRefill && maid.level().getGameTime() < SnackRaidData.getRetryAfter(maid)) {
            return;
        }
        if (!(maid.level() instanceof ServerLevel level)) {
            return;
        }
        SatietyData.ensureInitialized(maid);
        int satiety = SatietyData.getSatiety(maid);

        int radius = EatProperlyConfig.SNACK_RAID_RADIUS.get();
        int vertical = EatProperlyConfig.SNACK_RAID_VERTICAL_RANGE.get();

        // Fetching from a marked container is granted, not stolen: it answers to its own satiety
        // line and cooldown instead of the snack-raid toggles, day-long cooldown and backpack check.
        // N3.10/N3.10b: empty-mat picnic refill seeking bypasses satiety + Food Permit cooldown.
        if (PicnicRefillPolicy.allowsFoodPermitStart(seekingPicnicRefill, satiety, fetchSatietyThreshold())) {
            FoodPermitStart permitStart = findFoodPermitTarget(
                    level, maid, EatProperlyConfig.FOOD_PERMIT_SEARCH_RADIUS.get(), vertical,
                    seekingPicnicRefill);
            if (permitStart != null) {
                beginRaid(maid, permitStart.target(), true,
                        permitStart.plan().destination() == FoodPermitFetchPolicy.Destination.PICNIC);
                return;
            }
        }

        if (!EatProperlyConfig.SNACK_RAID_ENABLED.get()) {
            return;
        }
        if (!SnackRaidData.isAllowedLocally(maid)) {
            return;
        }
        if (satiety >= EatProperlyConfig.SATIETY_LOW_THRESHOLD.get()) {
            return;
        }
        if (SnackRaidFood.hasBackpackFood(maid)) {
            return;
        }
        if (!isOffCooldown(maid)) {
            return;
        }

        int maxContainers = EatProperlyConfig.SNACK_RAID_MAX_CONTAINERS_PER_SCAN.get();
        SnackRaidTarget target = SnackRaidContainers.findNearestTarget(
                level, maid, radius, vertical, maxContainers,
                SnackRaidData.getCachedChestPos(maid), markedPositions(level, maid));
        if (target == null) {
            SnackRaidData.clearCachedChestPos(maid);
            return;
        }

        beginRaid(maid, target, false, false);
    }

    /** Marked containers are hers to open, so she never sneaks from (and flees) them. */
    private static Set<BlockPos> markedPositions(ServerLevel level, EntityMaid maid) {
        Set<BlockPos> positions = new HashSet<>();
        for (FoodPermitMark mark : FoodPermitData.findEquippedMarks(maid)) {
            if (FoodPermitData.matchesDimension(mark, level)) {
                positions.add(FoodPermitData.toPos(mark));
            }
        }
        return positions;
    }

    /** Fetching starts as soon as she would sit down to a meal, not only when nearly starving. */
    private static int fetchSatietyThreshold() {
        return Math.max(
                EatProperlyConfig.EAT_WHEN_SATIETY_BELOW.get(),
                EatProperlyConfig.SATIETY_LOW_THRESHOLD.get());
    }

    private static void beginRaid(EntityMaid maid, SnackRaidTarget target, boolean foodPermitMode, boolean picnicMode) {
        SnackRaidData.setChestPos(maid, target.containerPos());
        SnackRaidData.setStandPos(maid, target.standPos());
        if (!foodPermitMode) {
            SnackRaidData.setCachedChestPos(maid, target.containerPos());
        }
        SnackRaidData.setFoodPermitMode(maid, foodPermitMode);
        SnackRaidData.setFoodPermitPicnicMode(maid, picnicMode);
        SnackRaidData.setPhaseTicks(maid, 0);
        SnackRaidData.setPhase(maid, SnackRaidPhase.APPROACH);
        SnackRaidPathLock.acquire(maid);
        SnackRaidPathing.walkToStand(
                maid, target.standPos(), target.containerPos(), approachSpeed(foodPermitMode));

        if (FoodPermitSpeechPolicy.speakPermitApproach(foodPermitMode)) {
            String line = MaidSpeech.formatFoodPermitApproach(maid, maid.getRandom());
            MaidSpeech.bubbleFoodPermitTrip(maid, line);
            return;
        }
        if (!SnackRaidApproachPolicy.shouldSpeakStealthApproach(foodPermitMode)) {
            return;
        }
        String line = MaidSpeech.formatSnackRaidApproach(maid, maid.getRandom());
        if (!line.isEmpty()) {
            MaidSpeech.bubbleToOwnerIfNear(maid, line);
        }
    }

    private static void tickActive(EntityMaid maid) {
        // Also acquires the lock for raids restored from saves made before the lock existed.
        SnackRaidPathLock.acquire(maid);
        if (SnackRaidPathLock.ownerTooFar(maid)) {
            abort(maid);
            return;
        }
        boolean foodPermitMode = SnackRaidData.isFoodPermitMode(maid);
        if ((!foodPermitMode && !EatProperlyConfig.SNACK_RAID_ENABLED.get())
                || maid.isSleeping() || !maid.canBrainMoving()) {
            abort(maid);
            return;
        }
        // Panicking is allowed to take the walk target away from us, so give up instead of tugging.
        if (maid.getLastHurtByMob() != null) {
            abort(maid);
            return;
        }
        SnackRaidPhase phase = SnackRaidData.getPhase(maid);
        int satietyThreshold = foodPermitMode
                ? fetchSatietyThreshold()
                : EatProperlyConfig.SATIETY_LOW_THRESHOLD.get();
        boolean satietyWouldAbort = SnackRaidFleePolicy.shouldAbortForSatiety(
                phase, SatietyData.getSatiety(maid), satietyThreshold);
        if (foodPermitMode) {
            boolean seekingPicnicRefill = PicnicRefillData.isSeeking(maid);
            if (PicnicRefillPolicy.shouldAbortPermitFetchForSatiety(seekingPicnicRefill, satietyWouldAbort)) {
                finish(maid);
                return;
            }
        } else if (satietyWouldAbort) {
            finish(maid);
            return;
        }
        if (foodPermitMode) {
            if (SnackRaidData.isFoodPermitPicnicMode(maid)) {
                if (picnicHasEffectivePoolFood(maid)) {
                    abort(maid);
                    return;
                }
            } else if (hasAnyEffectivePoolFoodInHandsOrBackpack(maid)) {
                abort(maid);
                return;
            }
        } else if (SnackRaidFood.hasBackpackFood(maid)) {
            abort(maid);
            return;
        }

        switch (phase) {
            case APPROACH -> tickApproach(maid);
            case WAIT -> tickWait(maid);
            case FLEE -> tickFlee(maid);
            default -> abort(maid);
        }
    }

    private static void tickApproach(EntityMaid maid) {
        BlockPos stand = SnackRaidData.getStandPos(maid);
        BlockPos chest = SnackRaidData.getChestPos(maid);
        if (stand.equals(BlockPos.ZERO) || chest.equals(BlockPos.ZERO)) {
            abort(maid);
            return;
        }

        int ticks = SnackRaidData.getPhaseTicks(maid) + 1;
        SnackRaidData.setPhaseTicks(maid, ticks);

        if (ticks > SnackRaidPathing.APPROACH_TIMEOUT_TICKS) {
            abort(maid);
            return;
        }

        SnackRaidPathing.faceBlock(maid, chest);
        if (SnackRaidArrival.arrivedAtStand(SnackRaidPathing.distanceSqTo(maid, stand))) {
            SnackRaidPathing.holdStill(maid);
            SnackRaidData.setPhase(maid, SnackRaidPhase.WAIT);
            SnackRaidData.setPhaseTicks(maid, 0);
            return;
        }

        if (ticks == 1 || ticks % SnackRaidPathing.REPATH_INTERVAL_TICKS == 0
                || SnackRaidPathing.isWalkTargetHijacked(maid, stand)) {
            SnackRaidPathing.walkToStand(
                    maid, stand, chest, approachSpeed(SnackRaidData.isFoodPermitMode(maid)));
        }
    }

    private static float approachSpeed(boolean foodPermitMode) {
        return SnackRaidApproachPolicy.approachSpeed(
                foodPermitMode, EatProperlyConfig.FOOD_PERMIT_APPROACH_SPEED.get());
    }

    private static void tickWait(EntityMaid maid) {
        BlockPos chest = SnackRaidData.getChestPos(maid);
        if (SnackRaidArrival.leftContainerDuringWait(SnackRaidPathing.distanceSqTo(maid, chest))) {
            abort(maid);
            return;
        }
        SnackRaidPathing.holdStill(maid);
        SnackRaidPathing.faceBlock(maid, chest);

        int ticks = SnackRaidData.getPhaseTicks(maid) + 1;
        SnackRaidData.setPhaseTicks(maid, ticks);

        if (ticks == SnackRaidPathing.WAIT_BEFORE_SOUND_TICKS) {
            playChestOpenSound(maid, chest);
        }
        if (ticks < SnackRaidPathing.WAIT_TOTAL_TICKS) {
            return;
        }

        if (!(maid.level() instanceof ServerLevel level)) {
            abort(maid);
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(chest);
        if (blockEntity == null) {
            abort(maid);
            return;
        }

        if (SnackRaidData.isFoodPermitMode(maid)) {
            int moved;
            if (SnackRaidData.isFoodPermitPicnicMode(maid)) {
                moved = FoodPermitTransfer.extractToBackpack(
                        blockEntity, maid, EatProperlyConfig.FOOD_PERMIT_MAX_TAKE_PICNIC.get());
            } else {
                moved = FoodPermitTransfer.extractToBackpack(
                        blockEntity, maid, EatProperlyConfig.FOOD_PERMIT_MAX_TAKE_BACKPACK.get());
            }
            if (moved <= 0) {
                abort(maid);
                return;
            }
            FoodPermitFetchData.setLastFetchTime(maid, maid.level().getGameTime());
            if (SnackRaidData.isFoodPermitPicnicMode(maid)) {
                FoodPermitFetchData.setPendingPicnicRefill(maid, true);
            }
            if (FoodPermitSpeechPolicy.speakPermitSuccess(true, moved)) {
                String line = MaidSpeech.formatFoodPermit(maid, maid.getRandom());
                MaidSpeech.bubbleFoodPermitTrip(maid, line);
            }
            finish(maid);
            return;
        }

        ItemStack taken = SnackRaidContainers.extractOneRaidFood(blockEntity, maid);
        if (taken.isEmpty()) {
            SnackRaidData.clearCachedChestPos(maid);
            abort(maid);
            return;
        }

        SnackRaidFood.applyNutrition(maid, taken);
        SnackRaidData.setLastRaidTime(maid, maid.level().getGameTime());
        SnackRaidData.setCachedChestPos(maid, chest);

        String line = MaidSpeech.formatSnackRaid(maid, maid.getRandom());
        if (!line.isEmpty()) {
            MaidSpeech.bubbleToOwnerIfNear(maid, line);
        }

        BlockPos flee = SnackRaidPathing.pickFleePos(level, maid, chest);
        if (flee != null) {
            SnackRaidData.setFleePos(maid, flee);
            SnackRaidData.setPhase(maid, SnackRaidPhase.FLEE);
            SnackRaidData.setPhaseTicks(maid, 0);
            SnackRaidPathing.walkToFlee(maid, flee);
        } else {
            finish(maid);
        }
    }

    private static void tickFlee(EntityMaid maid) {
        BlockPos flee = SnackRaidData.getFleePos(maid);
        if (flee.equals(BlockPos.ZERO)) {
            finish(maid);
            return;
        }

        int ticks = SnackRaidData.getPhaseTicks(maid) + 1;
        SnackRaidData.setPhaseTicks(maid, ticks);

        if (ticks > SnackRaidPathing.FLEE_TIMEOUT_TICKS) {
            finish(maid);
            return;
        }

        if (SnackRaidArrival.arrivedAtFlee(SnackRaidPathing.distanceSqTo(maid, flee))) {
            finish(maid);
            return;
        }

        // Only repath when navigation finished but we have not arrived (path lost / stuck).
        if (SnackRaidFleePolicy.shouldRepathWhileFleeing(
                maid.getNavigation().isDone(), ticks, SnackRaidPathing.REPATH_INTERVAL_TICKS)
                || SnackRaidPathing.isWalkTargetHijacked(maid, flee)) {
            SnackRaidPathing.walkToFlee(maid, flee);
        }
    }

    private static boolean isOffCooldown(EntityMaid maid) {
        long now = maid.level().getGameTime();
        long last = SnackRaidData.getLastRaidTime(maid);
        if (last <= 0L) {
            return true;
        }
        return now - last >= EatProperlyConfig.SNACK_RAID_COOLDOWN_TICKS.get();
    }

    private static FoodPermitStart findFoodPermitTarget(
            ServerLevel level, EntityMaid maid, int radius, int verticalRange, boolean picnicRefillSeeking) {
        if (!PicnicRefillPolicy.shouldBypassFoodPermitCooldown(picnicRefillSeeking)
                && !FoodPermitFetchPolicy.isOffCooldown(
                maid.level().getGameTime(),
                FoodPermitFetchData.getLastFetchTime(maid),
                EatProperlyConfig.FOOD_PERMIT_COOLDOWN_TICKS.get())) {
            return null;
        }
        boolean resting = isHomeMealTask(maid);
        boolean picnicHasFood = picnicHasEffectivePoolFood(maid);
        FetchPlan plan = FoodPermitFetchPolicy.decide(
                resting, picnicHasFood, hasAnyEffectivePoolFoodInHandsOrBackpack(maid), picnicRefillSeeking);
        if (plan.destination() == FoodPermitFetchPolicy.Destination.NONE) {
            return null;
        }
        SnackRaidTarget target = FoodPermitTargets.findNearestMarkedTarget(
                level, maid, FoodPermitData.findEquippedMarks(maid), radius, verticalRange);
        return target == null ? null : new FoodPermitStart(target, plan);
    }

    private static boolean hasAnyEffectivePoolFoodInHandsOrBackpack(EntityMaid maid) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (FoodPermitTargets.isEffectivePoolFood(maid.getItemInHand(hand), maid)) {
                return true;
            }
        }
        var inv = maid.getAvailableBackpackInv();
        for (int i = 0; i < inv.getSlots(); i++) {
            if (FoodPermitTargets.isEffectivePoolFood(inv.getStackInSlot(i), maid)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHomeMealTask(EntityMaid maid) {
        return maid.getTask() != null
                && MaidMealHungerPolicy.HOME_MEAL_TYPE.equals(String.valueOf(maid.getTask().getUid()));
    }

    private static boolean picnicHasEffectivePoolFood(EntityMaid maid) {
        IItemHandler handler = findPicnicBasketHandler(maid);
        if (handler == null) {
            return false;
        }
        for (int i = 0; i < handler.getSlots(); i++) {
            if (FoodPermitTargets.isEffectivePoolFood(handler.getStackInSlot(i), maid)) {
                return true;
            }
        }
        return false;
    }

    private static IItemHandler findPicnicBasketHandler(EntityMaid maid) {
        ItemStackHandler inv = maid.getMaidInv();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof ItemPicnicBasket) {
                return ItemPicnicBasket.getContainer(stack);
            }
        }
        return null;
    }

    private static void tryFlushPendingPicnicRefill(EntityMaid maid) {
        if (!FoodPermitFetchData.isPendingPicnicRefill(maid)) {
            return;
        }
        if (!isHomeMealTask(maid)) {
            return;
        }
        IItemHandler picnic = findPicnicBasketHandler(maid);
        if (picnic == null) {
            return;
        }
        int moved = FoodPermitTransfer.moveFromBackpackToItemHandler(
                picnic, maid, EatProperlyConfig.FOOD_PERMIT_MAX_TAKE_PICNIC.get());
        if (moved > 0 || picnicHasEffectivePoolFood(maid)) {
            FoodPermitFetchData.setPendingPicnicRefill(maid, false);
        }
    }

    private static void playChestOpenSound(EntityMaid maid, BlockPos chest) {
        if (!(maid.level() instanceof ServerLevel level)) {
            return;
        }
        level.playSound(null, chest, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS,
                0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    private static void abort(EntityMaid maid) {
        SnackRaidPathing.releaseControl(maid);
        SnackRaidData.setRetryAfter(maid, maid.level().getGameTime() + FAILED_RETRY_DELAY_TICKS);
        SnackRaidData.clearActive(maid);
    }

    private static void finish(EntityMaid maid) {
        SnackRaidPathing.releaseControl(maid);
        SnackRaidData.clearActive(maid);
    }
}
