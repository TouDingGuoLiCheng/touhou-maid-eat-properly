package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.api.task.meal.IMaidMeal;
import com.github.tartaricacid.touhoulittlemaid.api.task.meal.MaidMealType;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.MaidMealManager;
import com.github.tartaricacid.touhoulittlemaid.util.HandUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.util.List;

/**
 * Hunger-speech path helper: detect work-meal-eligible food (hands or backpack) and start one eat
 * the same way {@code MaidWorkMealTask} does (hand first, then pull from backpack).
 */
public final class HungerBackpackMeal {
    private HungerBackpackMeal() {
    }

    /** True when hands or backpack hold a stack the work-meal handlers would accept. */
    public static boolean hasEdibleFood(EntityMaid maid) {
        List<IMaidMeal> meals = MaidMealManager.getMaidMeals(MaidMealType.WORK_MEAL);
        for (InteractionHand hand : HandUtils.NATIVE_HANDS) {
            ItemStack held = maid.getItemInHand(hand);
            if (held.isEmpty()) {
                continue;
            }
            for (IMaidMeal meal : meals) {
                if (meal.canMaidEat(maid, held, hand)) {
                    return true;
                }
            }
        }
        CombinedInvWrapper inv = maid.getAvailableBackpackInv();
        InteractionHand probeHand = InteractionHand.MAIN_HAND;
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            for (IMaidMeal meal : meals) {
                if (meal.canMaidEat(maid, stack, probeHand)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Starts one work-meal eat from hands or backpack. Returns true when eating began.
     */
    public static boolean tryEatOne(EntityMaid maid) {
        List<IMaidMeal> meals = MaidMealManager.getMaidMeals(MaidMealType.WORK_MEAL);

        for (InteractionHand hand : HandUtils.NATIVE_HANDS) {
            ItemStack held = maid.getItemInHand(hand);
            if (held.isEmpty()) {
                continue;
            }
            for (IMaidMeal meal : meals) {
                if (meal.canMaidEat(maid, held, hand)) {
                    meal.onMaidEat(maid, held, hand);
                    return true;
                }
            }
        }

        InteractionHand emptyHand = InteractionHand.OFF_HAND;
        for (InteractionHand hand : HandUtils.NATIVE_HANDS) {
            if (maid.getItemInHand(hand).isEmpty()) {
                emptyHand = hand;
                break;
            }
        }
        if (!maid.getItemInHand(emptyHand).isEmpty()) {
            // Both hands occupied — cannot pull backpack food into a hand.
            return false;
        }

        CombinedInvWrapper inv = maid.getAvailableBackpackInv();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            for (IMaidMeal meal : meals) {
                if (!meal.canMaidEat(maid, stack, emptyHand)) {
                    continue;
                }
                ItemStack extracted = inv.extractItem(i, stack.getCount(), false);
                ItemStack previous = maid.getItemInHand(emptyHand).copy();
                maid.setItemInHand(emptyHand, extracted);
                maid.memoryHandItemStack(previous);
                meal.onMaidEat(maid, maid.getItemInHand(emptyHand), emptyHand);
                return true;
            }
        }
        return false;
    }
}
