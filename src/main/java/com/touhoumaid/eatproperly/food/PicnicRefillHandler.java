package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * N3.10: keep a resting home-meal maid from starving on an empty picnic mat.
 * Tops the seated mat up from what she carries; if she carries nothing, dismounts her so the
 * existing Food Permit fetch can run, then steers her back to the original mat to flush.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class PicnicRefillHandler {
    private static final int TARGET_SCAN_INTERVAL_TICKS = 20;

    private PicnicRefillHandler() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EntityMaid maid)) {
            return;
        }
        if (!maid.isAlive() || !maid.isTame()) {
            return;
        }
        if (!(maid.level() instanceof ServerLevel level)) {
            return;
        }

        // Last picnic slot: HomeMeal extracts food into the hand and starts using it; the mat is
        // briefly empty. Flushing that hand would put the food back and cancel the real eat.
        if (PicnicRefillPolicy.shouldDeferWhileUsingItem(maid.isUsingItem())) {
            return;
        }

        TileEntityPicnicMat mat = PicnicRefill.findSeatedHomeMealMat(maid);
        if (mat == null) {
            clearSeekIfDone(maid, level);
            return;
        }

        BlockPos matPos = mat.getBlockPos();
        boolean seeking = PicnicRefillData.isSeeking(maid);
        if (seeking && PicnicRefillData.hasHomeMat(maid) && !PicnicRefillData.matchesHomeMat(maid, level, matPos)) {
            // Wrong mat while returning — get up and keep seeking the original.
            maid.stopRiding();
            return;
        }

        if (PicnicRefill.matHasPoolFood(maid, mat)) {
            PicnicRefillData.clearSeeking(maid);
            return;
        }

        boolean carries = PicnicRefill.carriesPoolFood(maid);
        boolean reachable = !carries && hasReachableMarkedTarget(level, maid);
        switch (PicnicRefillPolicy.decide(true, false, carries, reachable)) {
            case FLUSH_IN_PLACE -> {
                PicnicRefill.flushCarriedFoodInto(maid, mat, EatProperlyConfig.FOOD_PERMIT_MAX_TAKE_PICNIC.get());
                PicnicRefillData.clearSeeking(maid);
            }
            case LEAVE_TO_FETCH -> {
                PicnicRefillData.setSeeking(maid, level.getGameTime(), matPos, level.dimension().location());
                maid.stopRiding();
            }
            default -> {
            }
        }
    }

    private static void clearSeekIfDone(EntityMaid maid, ServerLevel level) {
        if (!PicnicRefillData.isSeeking(maid)) {
            return;
        }
        boolean homeMissing = PicnicRefillData.isHomeMatMissing(maid, level);
        if (PicnicRefillPolicy.shouldClearSeek(
                false,
                homeMissing,
                level.getGameTime(),
                PicnicRefillData.getSeekSince(maid),
                PicnicRefillPolicy.SEEK_TIMEOUT_TICKS)) {
            PicnicRefillData.clearSeeking(maid);
        }
    }

    private static boolean hasReachableMarkedTarget(ServerLevel level, EntityMaid maid) {
        // Food Permit leave-to-fetch must not depend on the snack-raid master toggle.
        if (FoodPermitData.findEquippedMarks(maid).isEmpty()) {
            return false;
        }
        if ((level.getGameTime() + maid.getId()) % TARGET_SCAN_INTERVAL_TICKS != 0) {
            return false;
        }
        int radius = EatProperlyConfig.FOOD_PERMIT_SEARCH_RADIUS.get();
        int vertical = EatProperlyConfig.SNACK_RAID_VERTICAL_RANGE.get();
        return FoodPermitTargets.findNearestMarkedTarget(
                level, maid, FoodPermitData.findEquippedMarks(maid), radius, vertical) != null;
    }
}
