package com.touhoumaid.eatproperly.item.reward;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.advancement.EatProperlyAdvancements;
import com.touhoumaid.eatproperly.favorability.RequestFavorPolicy;
import com.touhoumaid.eatproperly.food.FoodRequestHandler;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import com.touhoumaid.eatproperly.food.OutOfPoolAngerData;
import com.touhoumaid.eatproperly.food.OutOfPoolDelivery;
import com.touhoumaid.eatproperly.food.MaidFoodReceive;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Right-click-a-maid behavior for the dual lunchbox, extra meal permit and hypnosis note.
 * The (single-slot) bound lunchbox is handled inline in {@code LunchboxEvents} since it extends
 * {@code LunchboxItem} and must share that exact same delivery path plus a bind check.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class CareItemEvents {
    private CareItemEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteractMaid(InteractMaidEvent event) {
        ItemStack stack = event.getStack();
        Item item = stack.getItem();
        if (item instanceof DualLunchboxItem) {
            onDualLunchbox(event, stack);
        } else if (item instanceof ExtraMealPermitItem) {
            onExtraMealPermit(event, stack);
        } else if (item instanceof HypnosisNoteItem) {
            onHypnosisNote(event, stack);
        }
    }

    private static void onDualLunchbox(InteractMaidEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) {
            event.setCanceled(true);
            return;
        }
        EntityMaid maid = event.getMaid();
        if (!maid.isOwnedBy(player)) {
            return;
        }

        ItemStack foodA = DualLunchboxItem.getStoredFood(stack, 0);
        ItemStack foodB = DualLunchboxItem.getStoredFood(stack, 1);
        if (foodA.isEmpty() && foodB.isEmpty()) {
            // Nothing to deliver; let the default maid interaction (e.g. normal GUI) proceed.
            return;
        }

        ItemStack foodACopy = foodA.copy();
        ItemStack foodBCopy = foodB.copy();
        boolean wantDoubleComplete = !foodACopy.isEmpty() && !foodBCopy.isEmpty()
                && FoodRequestHandler.canCompleteDual(maid, foodACopy, foodBCopy);

        boolean delivered = deliverDualRespectingAnger(stack, maid);
        if (delivered) {
            boolean aDelivered = foodACopy.isEmpty() || DualLunchboxItem.getStoredFood(stack, 0).isEmpty();
            boolean bDelivered = foodBCopy.isEmpty() || DualLunchboxItem.getStoredFood(stack, 1).isEmpty();
            if (wantDoubleComplete && aDelivered && bDelivered) {
                FoodRequestHandler.completeCurrentRequestWithPoints(maid, RequestFavorPolicy.DUAL_POINTS);
            } else {
                FoodRequestHandler.tryCompleteFromCarriedNow(maid);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                EatProperlyAdvancements.onLunchboxDelivered(serverPlayer);
            }
            player.getCooldowns().addCooldown(stack.getItem(), 10);
        } else if (DualLunchboxItem.isMaidInvTooFullFor(stack, maid)) {
            MaidSpeech.bubbleToOwnerIfNear(maid, "主人，我腾不出手来吃东西啦！");
        }
        event.setCanceled(true);
    }

    /**
     * In-pool slots first (may forgive); out-of-pool never delivers if the maid was angry
     * at the start of this hand-in (forgive must not unlock out-of-pool in the same action).
     * All attempted slots must fully fit into the visible backpack or nothing is moved.
     */
    private static boolean deliverDualRespectingAnger(ItemStack dual, EntityMaid maid) {
        ItemStackHandler handler = DualLunchboxItem.getContainer(dual);
        long now = maid.level().getGameTime();
        boolean angryAtStart = OutOfPoolAngerData.isAngry(maid, now);
        boolean attemptedOutWhileAngry = false;

        List<Integer> toMove = new ArrayList<>(DualLunchboxItem.SLOTS);
        for (int i = 0; i < DualLunchboxItem.SLOTS; i++) {
            ItemStack food = handler.getStackInSlot(i);
            if (food.isEmpty() || !OutOfPoolDelivery.isInEffectivePool(maid, food)) {
                continue;
            }
            toMove.add(i);
        }
        for (int i = 0; i < DualLunchboxItem.SLOTS; i++) {
            ItemStack food = handler.getStackInSlot(i);
            if (food.isEmpty() || OutOfPoolDelivery.isInEffectivePool(maid, food)) {
                continue;
            }
            if (angryAtStart || OutOfPoolDelivery.shouldBlockStow(maid, food)) {
                attemptedOutWhileAngry = true;
                continue;
            }
            toMove.add(i);
        }

        if (toMove.isEmpty()) {
            if (attemptedOutWhileAngry && OutOfPoolAngerData.isAngry(maid, maid.level().getGameTime())) {
                OutOfPoolDelivery.onBlockedAngry(maid);
            }
            return false;
        }

        List<ItemStack> foods = new ArrayList<>(toMove.size());
        for (int slot : toMove) {
            foods.add(handler.getStackInSlot(slot).copy());
        }
        if (!MaidFoodReceive.canFullyReceiveAll(maid, foods)) {
            return false;
        }

        boolean movedAny = false;
        for (int slot : toMove) {
            ItemStack food = handler.getStackInSlot(slot);
            if (food.isEmpty()) {
                continue;
            }
            if (tryMoveDualSlot(handler, slot, maid, food)) {
                movedAny = true;
            }
        }

        DualLunchboxItem.setContainer(dual, handler);
        if (attemptedOutWhileAngry && OutOfPoolAngerData.isAngry(maid, maid.level().getGameTime())) {
            OutOfPoolDelivery.onBlockedAngry(maid);
        }
        return movedAny;
    }

    private static boolean tryMoveDualSlot(
            ItemStackHandler handler, int slot, EntityMaid maid, ItemStack food) {
        ItemStack foodCopy = food.copy();
        if (!MaidFoodReceive.canFullyReceive(maid, food)) {
            return false;
        }
        if (!MaidFoodReceive.tryFullyReceive(maid, food)) {
            return false;
        }
        handler.extractItem(slot, food.getCount(), false);
        OutOfPoolDelivery.afterSuccessfulStow(maid, foodCopy, foodCopy.getCount());
        return true;
    }

    private static void onExtraMealPermit(InteractMaidEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) {
            event.setCanceled(true);
            return;
        }
        EntityMaid maid = event.getMaid();
        if (!maid.isOwnedBy(player)) {
            return;
        }
        boolean success = FoodRequestHandler.forceNewRequest(maid);
        if (success) {
            stack.shrink(1);
        } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.touhou_maid_eat_properly.extra_meal_permit_no_food"), true);
        }
        event.setCanceled(true);
    }

    private static void onHypnosisNote(InteractMaidEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) {
            event.setCanceled(true);
            return;
        }
        EntityMaid maid = event.getMaid();
        if (!maid.isOwnedBy(player)) {
            return;
        }
        if (FoodRequestHandler.forgetCurrentRequest(maid)) {
            stack.shrink(1);
            String speech = MaidSpeech.formatHypnosis(maid.getRandom());
            MaidSpeech.bubbleToOwnerIfNear(maid, speech);
        } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.touhou_maid_eat_properly.hypnosis_no_request"), true);
        }
        event.setCanceled(true);
    }
}
