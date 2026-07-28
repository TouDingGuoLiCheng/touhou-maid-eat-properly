package com.touhoumaid.eatproperly.item.reward;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.advancement.EatProperlyAdvancements;
import com.touhoumaid.eatproperly.food.FoodRequestData;
import com.touhoumaid.eatproperly.food.FoodRequestHandler;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

        boolean doubleComplete = !foodA.isEmpty() && !foodB.isEmpty()
                && FoodRequestHandler.canCompleteDual(maid, foodA, foodB);

        boolean delivered = DualLunchboxItem.deliverAllTo(stack, maid.getMaidInv());
        if (delivered) {
            if (doubleComplete) {
                FoodRequestHandler.completeCurrentRequestWithLevelGain(maid, 2);
            } else {
                FoodRequestHandler.tryCompleteFromBackpackNow(maid);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                EatProperlyAdvancements.onLunchboxDelivered(serverPlayer);
            }
            player.getCooldowns().addCooldown(stack.getItem(), 10);
        }
        event.setCanceled(true);
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
        boolean hasRequest = FoodRequestData.hasRequest(maid);
        boolean success = hasRequest && !FoodRequestData.isForgotten(maid);
        if (success) {
            FoodRequestData.setForgotten(maid, true);
            stack.shrink(1);
            String speech = MaidSpeech.formatHypnosis(maid.getRandom());
            MaidSpeech.speakToOwner(maid, speech);
        } else if (player instanceof ServerPlayer serverPlayer) {
            String key = hasRequest
                    ? "message.touhou_maid_eat_properly.hypnosis_already_forgotten"
                    : "message.touhou_maid_eat_properly.hypnosis_no_request";
            serverPlayer.displayClientMessage(Component.translatable(key), true);
        }
        event.setCanceled(true);
    }
}
