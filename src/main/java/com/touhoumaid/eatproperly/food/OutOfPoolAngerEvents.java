package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.item.LunchboxItem;
import com.touhoumaid.eatproperly.item.reward.DualLunchboxItem;
import com.touhoumaid.eatproperly.item.reward.ExtraMealPermitItem;
import com.touhoumaid.eatproperly.item.reward.HypnosisNoteItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * While angry: block non-delivery maid GUI opens; allow in-pool hand-ins to forgive;
 * reject out-of-pool hand-ins without opening GUI.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class OutOfPoolAngerEvents {
    private OutOfPoolAngerEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void blockGuiWhileAngry(InteractMaidEvent event) {
        if (event.isCanceled() || event.getPlayer().level().isClientSide()) {
            return;
        }
        EntityMaid maid = event.getMaid();
        if (!maid.isOwnedBy(event.getPlayer())) {
            return;
        }
        if (isExemptFromGuiBlock(event.getStack())) {
            return;
        }
        if (OutOfPoolDelivery.tryBlockGui(maid)) {
            event.setCanceled(true);
        }
    }

    /**
     * Angry + held edible: stow in-pool (forgive) or refuse out-of-pool. Runs after lunchbox handlers.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onHandFoodWhileAngry(InteractMaidEvent event) {
        if (event.isCanceled() || event.getPlayer().level().isClientSide()) {
            return;
        }
        ItemStack stack = event.getStack();
        if (stack.isEmpty() || !stack.isEdible()
                || stack.getItem() instanceof LunchboxItem
                || stack.getItem() instanceof DualLunchboxItem
                || stack.getItem() instanceof ExtraMealPermitItem
                || stack.getItem() instanceof HypnosisNoteItem) {
            return;
        }
        EntityMaid maid = event.getMaid();
        if (!maid.isOwnedBy(event.getPlayer())) {
            return;
        }
        long now = maid.level().getGameTime();
        if (!OutOfPoolAngerData.isAngry(maid, now)) {
            return;
        }
        if (OutOfPoolDelivery.shouldBlockStow(maid, stack)) {
            OutOfPoolDelivery.onBlockedAngry(maid);
            event.setCanceled(true);
            return;
        }
        ItemStack one = stack.copyWithCount(1);
        if (!MaidFoodReceive.tryFullyReceive(maid, one)) {
            event.setCanceled(true);
            return;
        }
        stack.shrink(1);
        OutOfPoolDelivery.afterSuccessfulStow(maid, one, 1);
        FoodRequestHandler.tryCompleteFromCarriedNow(maid);
        event.setCanceled(true);
    }

    /** Items that have their own InteractMaidEvent handlers (must not be GUI-blocked here). */
    private static boolean isExemptFromGuiBlock(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof LunchboxItem) {
            return true;
        }
        if (stack.getItem() instanceof DualLunchboxItem) {
            return !DualLunchboxItem.getStoredFood(stack, 0).isEmpty()
                    || !DualLunchboxItem.getStoredFood(stack, 1).isEmpty();
        }
        if (stack.getItem() instanceof ExtraMealPermitItem || stack.getItem() instanceof HypnosisNoteItem) {
            return true;
        }
        return stack.isEdible();
    }
}
