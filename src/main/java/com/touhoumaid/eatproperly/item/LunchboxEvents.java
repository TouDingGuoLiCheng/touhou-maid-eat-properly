package com.touhoumaid.eatproperly.item;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.advancement.EatProperlyAdvancements;
import com.touhoumaid.eatproperly.food.FoodRequestHandler;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import com.touhoumaid.eatproperly.food.OutOfPoolDelivery;
import com.touhoumaid.eatproperly.item.reward.BoundLunchboxItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class LunchboxEvents {
    private LunchboxEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteractMaid(InteractMaidEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getStack();
        if (!(stack.getItem() instanceof LunchboxItem)) {
            return;
        }
        if (player.level().isClientSide()) {
            event.setCanceled(true);
            return;
        }

        EntityMaid maid = event.getMaid();
        if (!maid.isOwnedBy(player)) {
            return;
        }

        ItemStack food = LunchboxItem.getStoredFood(stack);
        if (food.isEmpty()) {
            // Empty box: open GUI instead of maid panel (bind check does not apply here).
            if (player instanceof ServerPlayer serverPlayer) {
                LunchboxItem.openGui(serverPlayer, stack);
            }
            event.setCanceled(true);
            return;
        }

        if (stack.getItem() instanceof BoundLunchboxItem && !BoundLunchboxItem.matchesBoundMaid(stack, maid)) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.touhou_maid_eat_properly.bound_lunchbox_wrong_maid"), true);
            }
            event.setCanceled(true);
            return;
        }

        if (OutOfPoolDelivery.shouldBlockStow(maid, food)) {
            OutOfPoolDelivery.onBlockedAngry(maid);
            event.setCanceled(true);
            return;
        }

        ItemStack foodCopy = food.copy();
        // Visible backpack first, then a free hand — reject only when neither can take the stack.
        boolean delivered = LunchboxItem.deliverTo(stack, maid);
        if (delivered) {
            OutOfPoolDelivery.afterSuccessfulStow(maid, foodCopy, foodCopy.getCount());
            FoodRequestHandler.tryCompleteFromCarriedNow(maid);
            if (player instanceof ServerPlayer serverPlayer) {
                EatProperlyAdvancements.onLunchboxDelivered(serverPlayer);
            }
            player.getCooldowns().addCooldown(stack.getItem(), 10);
        } else {
            MaidSpeech.bubbleToOwnerIfNear(maid, "主人，我腾不出手来吃东西啦！");
        }
        event.setCanceled(true);
    }
}
