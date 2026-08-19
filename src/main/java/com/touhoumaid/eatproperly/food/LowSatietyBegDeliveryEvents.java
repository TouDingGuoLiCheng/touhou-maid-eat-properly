package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.item.LunchboxItem;
import com.touhoumaid.eatproperly.item.reward.DualLunchboxItem;
import com.touhoumaid.eatproperly.item.reward.ExtraMealPermitItem;
import com.touhoumaid.eatproperly.item.reward.HypnosisNoteItem;
import com.touhoumaid.eatproperly.satiety.LowSatietyBegData;
import com.touhoumaid.eatproperly.satiety.LowSatietyBegHandler;
import com.touhoumaid.eatproperly.satiety.LowSatietyBegPolicy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * N1: while low-satiety begging, right-click with any effective-pool food stows 1 into the maid;
 * matching the current request also completes it. Out-of-pool food while begging also stows
 * (refuse / anger path) unless the maid is already angry.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class LowSatietyBegDeliveryEvents {
    private LowSatietyBegDeliveryEvents() {
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onBegGiveFood(InteractMaidEvent event) {
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
        Player player = event.getPlayer();
        EntityMaid maid = event.getMaid();
        if (!maid.isOwnedBy(player)) {
            return;
        }
        if (!stack.isEdible()) {
            return;
        }

        boolean begging = LowSatietyBegData.isActive(maid);
        boolean inPool = LowSatietyBegHandler.isHeldPoolFood(maid, stack);
        boolean matches = FoodRequestData.hasRequest(maid)
                && FoodRequestHandler.matchesRequest(stack, FoodRequestData.getRequestStack(maid));
        LowSatietyBegPolicy.GiveAction action = LowSatietyBegPolicy.decideGive(begging, inPool, matches);

        // Begging + out-of-pool: stow with refuse/anger (or block while angry).
        if (action == LowSatietyBegPolicy.GiveAction.REJECT) {
            if (!begging || inPool) {
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
            event.setCanceled(true);
            return;
        }

        ItemStack one = stack.copyWithCount(1);
        if (!MaidFoodReceive.tryFullyReceive(maid, one)) {
            event.setCanceled(true);
            return;
        }
        stack.shrink(1);
        if (action == LowSatietyBegPolicy.GiveAction.COMPLETE_REQUEST) {
            FoodRequestHandler.tryCompleteFromCarriedNow(maid, false);
        }
        boolean sameFood = LowSatietyBegData.isSameAsBeggedFood(maid, FoodPool.toId(one));
        long now = maid.level().getGameTime();
        boolean wasAngry = OutOfPoolAngerData.isAngry(maid, now);
        OutOfPoolDelivery.afterSuccessfulStow(maid, one, 1);
        if (!wasAngry) {
            String thanks = sameFood
                    ? MaidSpeech.formatLowSatietyBegThanks(one, maid.getRandom())
                    : MaidSpeech.formatLowSatietyBegThanksOther(one, maid.getRandom());
            if (!thanks.isEmpty()) {
                MaidSpeech.bubbleToOwnerIfNear(maid, thanks);
            }
        }
        event.setCanceled(true);
    }
}
