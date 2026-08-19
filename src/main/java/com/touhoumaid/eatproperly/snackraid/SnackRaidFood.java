package com.touhoumaid.eatproperly.snackraid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.food.FoodPool;
import com.touhoumaid.eatproperly.food.FoodPoolSafety;
import com.touhoumaid.eatproperly.food.OrderBookData;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * Food eligibility and satiety restoration for snack raids / “already has food” gates.
 * Carried-food checks use the maid's <b>effective</b> pool and only hands + available backpack
 * slots (not the full 36-slot {@code getMaidInv()}, which can hide leftovers outside the GUI).
 */
public final class SnackRaidFood {
    private SnackRaidFood() {
    }

    public static boolean isRaidFood(ItemStack stack, LivingEntity eater) {
        if (stack.isEmpty()) {
            return false;
        }
        if (eater instanceof EntityMaid maid) {
            if (!FoodPool.isInEffectivePool(stack, OrderBookData.findAllEquipped(maid))) {
                return false;
            }
        } else if (!FoodPool.isPoolItem(stack)) {
            return false;
        }
        FoodProperties food = stack.getFoodProperties(eater);
        if (food == null || food.getNutrition() <= 0) {
            return false;
        }
        return !FoodPoolSafety.hasHarmfulFoodEffect(food);
    }

    /**
     * True when hands or the currently available backpack hold effective-pool raid food.
     * Used to skip starting a snack raid / as the status “has food” line.
     */
    public static boolean hasBackpackFood(EntityMaid maid) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (isRaidFood(maid.getItemInHand(hand), maid)) {
                return true;
            }
        }
        IItemHandler inv = maid.getAvailableBackpackInv();
        for (int i = 0; i < inv.getSlots(); i++) {
            if (isRaidFood(inv.getStackInSlot(i), maid)) {
                return true;
            }
        }
        return false;
    }

    public static void applyNutrition(EntityMaid maid, ItemStack eaten) {
        FoodProperties food = eaten.getFoodProperties(maid);
        if (food == null) {
            return;
        }
        SatietyData.eatFood(maid, food.getNutrition(), food.getSaturationModifier());
    }
}
