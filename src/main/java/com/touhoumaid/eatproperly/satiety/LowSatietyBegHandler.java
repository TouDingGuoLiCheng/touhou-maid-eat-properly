package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.food.AngerAttractionPolicy;
import com.touhoumaid.eatproperly.food.FoodPool;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import com.touhoumaid.eatproperly.food.OrderBookData;
import com.touhoumaid.eatproperly.food.OrderBookSnapshot;
import com.touhoumaid.eatproperly.food.OutOfPoolAngerData;
import com.touhoumaid.eatproperly.snackraid.SnackRaidData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * N1: when hungry, empty-handed (no work-meal food), and the owner holds pool food nearby,
 * set begging and approach / look at the owner.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class LowSatietyBegHandler {
    private LowSatietyBegHandler() {
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
        boolean wasActive = LowSatietyBegData.isActive(maid);
        if (!wasActive && maid.tickCount % 20 != 0) {
            return;
        }
        tick(maid, wasActive);
    }

    private static void tick(EntityMaid maid, boolean wasActive) {
        boolean should = shouldBeg(maid);
        if (should) {
            if (!wasActive) {
                LowSatietyBegData.setActive(maid, true);
                ItemStack heldFood = findOwnerPoolFood(maid, ownerFrom(maid));
                LowSatietyBegData.setBeggedFoodId(maid, FoodPool.toId(heldFood));
                String text = MaidSpeech.formatLowSatietyBeg(heldFood, maid.getRandom());
                if (!text.isEmpty()) {
                    MaidSpeech.bubbleToOwnerIfNear(maid, text);
                }
            }
            maid.setBegging(true);
            LivingEntity owner = maid.getOwner();
            if (owner instanceof Player player) {
                LowSatietyBegPathing.approachOwner(maid, player);
            }
            return;
        }
        if (wasActive) {
            LowSatietyBegData.setActive(maid, false);
            LowSatietyBegPathing.clearApproach(maid);
            maid.setBegging(false);
        }
    }

    static boolean shouldBeg(EntityMaid maid) {
        if (SnackRaidData.isActive(maid)) {
            return false;
        }
        if (maid.isSleeping() || !maid.canBrainMoving()) {
            return false;
        }
        long now = maid.level().getGameTime();
        if (!AngerAttractionPolicy.shouldAllowAttraction(OutOfPoolAngerData.isAngry(maid, now))) {
            return false;
        }
        SatietyData.ensureInitialized(maid);
        if (!LowSatietyBegPolicy.satietyAllows(SatietyData.getSatiety(maid))) {
            return false;
        }
        if (HungerBackpackMeal.hasEdibleFood(maid)) {
            return false;
        }
        LivingEntity ownerEntity = maid.getOwner();
        if (!(ownerEntity instanceof Player owner) || !owner.isAlive()) {
            return false;
        }
        if (!LowSatietyBegPolicy.ownerInRange(maid.distanceTo(owner))) {
            return false;
        }
        return ownerHoldsEffectivePoolFood(maid, owner);
    }

    static boolean ownerHoldsEffectivePoolFood(EntityMaid maid, Player owner) {
        return !findOwnerPoolFood(maid, owner).isEmpty();
    }

    /** Main hand first, then offhand; empty if neither is in the effective pool. */
    static ItemStack findOwnerPoolFood(EntityMaid maid, Player owner) {
        if (owner == null) {
            return ItemStack.EMPTY;
        }
        List<OrderBookSnapshot> books = OrderBookData.findAllEquipped(maid);
        ItemStack main = owner.getMainHandItem();
        if (FoodPool.isInEffectivePool(main, books)) {
            return main;
        }
        ItemStack off = owner.getOffhandItem();
        if (FoodPool.isInEffectivePool(off, books)) {
            return off;
        }
        return ItemStack.EMPTY;
    }

    private static Player ownerFrom(EntityMaid maid) {
        LivingEntity owner = maid.getOwner();
        return owner instanceof Player player ? player : null;
    }

    /** Whether {@code stack} is in this maid's effective pool (for delivery). */
    public static boolean isHeldPoolFood(EntityMaid maid, ItemStack stack) {
        return FoodPool.isInEffectivePool(stack, OrderBookData.findAllEquipped(maid));
    }
}
