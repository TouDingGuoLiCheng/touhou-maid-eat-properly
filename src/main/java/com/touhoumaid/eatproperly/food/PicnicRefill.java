package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import com.github.tartaricacid.touhoulittlemaid.util.HandUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Resting home-meal picnic-mat refill helpers (N3.10).
 *
 * <p>Targets exactly the mat the maid eats from: {@code MaidHomeMealTask} reads the tile at the
 * seated {@link EntitySit}'s associated block, so we top up that same handler.
 */
public final class PicnicRefill {
    private PicnicRefill() {
    }

    /** The picnic mat the maid currently rests on for a home meal, or {@code null}. */
    public static TileEntityPicnicMat findSeatedHomeMealMat(EntityMaid maid) {
        Entity vehicle = maid.getVehicle();
        if (!(vehicle instanceof EntitySit sit)) {
            return null;
        }
        if (!Type.ON_HOME_MEAL.getTypeName().equals(sit.getJoyType())) {
            return null;
        }
        Level level = maid.level();
        BlockPos pos = sit.getAssociatedBlockPos();
        if (level == null || pos == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof TileEntityPicnicMat mat ? mat : null;
    }

    public static boolean isRestingOnHomeMeal(EntityMaid maid) {
        return findSeatedHomeMealMat(maid) != null;
    }

    public static boolean matHasPoolFood(EntityMaid maid, TileEntityPicnicMat mat) {
        if (mat == null) {
            return false;
        }
        IItemHandler handler = mat.getHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            if (FoodPermitTargets.isEffectivePoolFood(handler.getStackInSlot(i), maid)) {
                return true;
            }
        }
        return false;
    }

    public static boolean carriesPoolFood(EntityMaid maid) {
        for (InteractionHand hand : HandUtils.NATIVE_HANDS) {
            if (FoodPermitTargets.isEffectivePoolFood(maid.getItemInHand(hand), maid)) {
                return true;
            }
        }
        IItemHandler inv = maid.getAvailableBackpackInv();
        for (int i = 0; i < inv.getSlots(); i++) {
            if (FoodPermitTargets.isEffectivePoolFood(inv.getStackInSlot(i), maid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moves up to {@code maxTake} carried effective-pool food items into the mat handler.
     * Hands are drained first (so bowls do not stay stuck), then the backpack. Returns moved count.
     */
    public static int flushCarriedFoodInto(EntityMaid maid, TileEntityPicnicMat mat, int maxTake) {
        if (mat == null || maxTake <= 0) {
            return 0;
        }
        IItemHandler dest = mat.getHandler();
        int moved = 0;
        for (InteractionHand hand : HandUtils.NATIVE_HANDS) {
            if (moved >= maxTake) {
                break;
            }
            ItemStack held = maid.getItemInHand(hand);
            if (!FoodPermitTargets.isEffectivePoolFood(held, maid)) {
                continue;
            }
            moved += drainHandInto(dest, maid, hand, maxTake - moved);
        }
        IItemHandler backpack = maid.getAvailableBackpackInv();
        for (int i = 0; i < backpack.getSlots() && moved < maxTake; i++) {
            ItemStack stack = backpack.getStackInSlot(i);
            if (!FoodPermitTargets.isEffectivePoolFood(stack, maid)) {
                continue;
            }
            moved += drainHandlerSlotInto(dest, backpack, i, maxTake - moved);
        }
        if (moved > 0) {
            mat.refresh();
        }
        return moved;
    }

    private static int drainHandInto(IItemHandler dest, EntityMaid maid, InteractionHand hand, int limit) {
        ItemStack held = maid.getItemInHand(hand);
        if (held.isEmpty() || limit <= 0) {
            return 0;
        }
        int moved = moveStackInto(dest, held.copy(), limit);
        if (moved > 0) {
            ItemStack remaining = held.copy();
            remaining.shrink(moved);
            maid.setItemInHand(hand, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
        }
        return moved;
    }

    private static int drainHandlerSlotInto(IItemHandler dest, IItemHandler source, int slot, int limit) {
        ItemStack stack = source.getStackInSlot(slot);
        if (stack.isEmpty() || limit <= 0) {
            return 0;
        }
        int moved = moveStackInto(dest, stack.copy(), limit);
        if (moved > 0) {
            source.extractItem(slot, moved, false);
        }
        return moved;
    }

    private static int moveStackInto(IItemHandler dest, ItemStack working, int limit) {
        int moved = 0;
        while (moved < limit && !working.isEmpty()) {
            ItemStack one = working.copyWithCount(1);
            ItemStack leftover = ItemHandlerHelper.insertItemStacked(dest, one, false);
            if (!leftover.isEmpty()) {
                break;
            }
            working.shrink(1);
            moved++;
        }
        return moved;
    }
}
