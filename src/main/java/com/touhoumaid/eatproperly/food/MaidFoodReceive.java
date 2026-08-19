package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.HandUtils;
import com.touhoumaid.eatproperly.item.LunchboxInvInsert;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

/**
 * Unified maid-side capacity for food delivery and leftover stow: visible backpack first, then an
 * empty hand. Backpack food is what meal AI and request completion look at, so hands are only a
 * fallback for a full bag — the maid can eat with both hands occupied anyway.
 */
public final class MaidFoodReceive {
    private MaidFoodReceive() {
    }

    public static boolean hasEmptyHand(EntityMaid maid) {
        for (InteractionHand hand : HandUtils.NATIVE_HANDS) {
            if (maid.getItemInHand(hand).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean canFullyReceive(EntityMaid maid, ItemStack food) {
        if (food.isEmpty()) {
            return false;
        }
        ItemStack simulated = ItemHandlerHelper.insertItemStacked(
                maid.getAvailableBackpackInv(), food.copy(), true);
        return MaidFoodReceivePolicy.canFullyReceive(
                hasEmptyHand(maid), food.getCount(), simulated.getCount());
    }

    public static boolean shouldRejectDelivery(EntityMaid maid, ItemStack food) {
        if (food.isEmpty()) {
            return true;
        }
        ItemStack simulated = ItemHandlerHelper.insertItemStacked(
                maid.getAvailableBackpackInv(), food.copy(), true);
        return MaidFoodReceivePolicy.shouldRejectDelivery(
                maid.getMainHandItem().isEmpty(),
                maid.getOffhandItem().isEmpty(),
                food.getCount(),
                simulated.getCount());
    }

    public static boolean canFullyReceiveAll(EntityMaid maid, List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return false;
        }
        boolean mainEmpty = maid.getMainHandItem().isEmpty();
        boolean offEmpty = maid.getOffhandItem().isEmpty();
        ItemStackHandler sim = LunchboxInvInsert.copyHandler(maid.getAvailableBackpackInv());
        boolean any = false;
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            any = true;
            // Mirrors tryFullyReceive: a stack goes to the backpack only when it fits whole.
            if (ItemHandlerHelper.insertItemStacked(sim, stack.copy(), true).isEmpty()) {
                ItemHandlerHelper.insertItemStacked(sim, stack.copy(), false);
                continue;
            }
            if (mainEmpty) {
                mainEmpty = false;
                continue;
            }
            if (offEmpty) {
                offEmpty = false;
                continue;
            }
            return false;
        }
        return any;
    }

    /**
     * Places the full stack into the visible backpack, otherwise into the first empty hand.
     */
    public static boolean tryFullyReceive(EntityMaid maid, ItemStack food) {
        if (food.isEmpty()) {
            return false;
        }
        IItemHandler backpack = maid.getAvailableBackpackInv();
        if (ItemHandlerHelper.insertItemStacked(backpack, food.copy(), true).isEmpty()) {
            ItemHandlerHelper.insertItemStacked(backpack, food.copy(), false);
            return true;
        }
        for (InteractionHand hand : HandUtils.NATIVE_HANDS) {
            if (maid.getItemInHand(hand).isEmpty()) {
                maid.setItemInHand(hand, food.copy());
                return true;
            }
        }
        return false;
    }

}
