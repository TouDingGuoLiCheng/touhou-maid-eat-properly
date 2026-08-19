package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.AbstractMaidContainer;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.other.PicnicBasketContainer;
import com.github.tartaricacid.touhoulittlemaid.item.ItemPicnicBasket;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.item.LunchboxItem;
import com.touhoumaid.eatproperly.item.reward.DualLunchboxItem;
import com.touhoumaid.eatproperly.item.reward.ExtraMealPermitItem;
import com.touhoumaid.eatproperly.item.reward.HypnosisNoteItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;

/**
 * Event-driven food-request completion: maid GUI close, picnic basket close,
 * picnic-mat food place, and right-click giving edible food.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class FoodRequestDeliveryEvents {
    private static final double OWNED_MAID_SCAN_RANGE = 64.0D;

    private FoodRequestDeliveryEvents() {
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (event.getContainer() instanceof AbstractMaidContainer maidMenu) {
            EntityMaid maid = maidMenu.getMaid();
            if (maid != null) {
                FoodRequestHandler.tryCompleteFromCarriedNow(maid);
            }
            return;
        }
        if (event.getContainer() instanceof PicnicBasketContainer) {
            ItemStack basket = player.getMainHandItem();
            if (basket.getItem() instanceof ItemPicnicBasket) {
                tryCompleteOwnedMaidsFromHandler(player, ItemPicnicBasket.getContainer(basket));
            }
        }
    }

    /**
     * Right-click a maid while holding the requested food: put one into her backpack and complete.
     * Runs after lunchbox / care-item handlers (those cancel the event when they handle it).
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGiveRequestedFood(InteractMaidEvent event) {
        if (event.isCanceled() || event.getPlayer().level().isClientSide()) {
            return;
        }
        ItemStack stack = event.getStack();
        if (stack.isEmpty() || stack.getItem() instanceof LunchboxItem
                || stack.getItem() instanceof DualLunchboxItem
                || stack.getItem() instanceof ExtraMealPermitItem
                || stack.getItem() instanceof HypnosisNoteItem) {
            return;
        }
        if (!stack.isEdible()) {
            return;
        }
        Player player = event.getPlayer();
        EntityMaid maid = event.getMaid();
        if (!maid.isOwnedBy(player) || !FoodRequestData.hasRequest(maid)) {
            return;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty() || !FoodRequestHandler.matchesRequest(stack, wanted)) {
            return;
        }
        ItemStack one = stack.copyWithCount(1);
        if (!MaidFoodReceive.tryFullyReceive(maid, one)) {
            return;
        }
        stack.shrink(1);
        FoodRequestHandler.tryCompleteFromCarriedNow(maid);
        event.setCanceled(true);
    }

    /** Completes requests for the player's nearby owned maids when a picnic handler gains matching food. */
    public static void tryCompleteOwnedMaidsFromHandler(Player player, IItemHandler handler) {
        if (player == null || handler == null || player.level().isClientSide()) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        for (EntityMaid maid : level.getEntitiesOfClass(
                EntityMaid.class, player.getBoundingBox().inflate(OWNED_MAID_SCAN_RANGE))) {
            if (!maid.isAlive() || !maid.isOwnedBy(player)) {
                continue;
            }
            FoodRequestHandler.tryCompleteFromItemHandler(maid, handler);
        }
    }

    /** After food is placed onto a picnic mat. */
    public static void onPicnicMatFoodPlaced(Player player, ItemStackHandler matHandler) {
        tryCompleteOwnedMaidsFromHandler(player, matHandler);
    }
}
