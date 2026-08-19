package com.touhoumaid.eatproperly.food;

import com.touhoumaid.eatproperly.snackraid.SnackRaidContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * N3.6-ghost / N3.6g-count: drop Food Permit marks whose block is no longer a markable container.
 * Called when a maid scans marks for Food Permit fetch, and when a player holds the permit
 * (selected hotbar slot, throttled). Other-dimension marks are left alone until that dimension
 * is the prune context.
 */
public final class FoodPermitPrune {
    private FoodPermitPrune() {
    }

    /**
     * Prunes marks for {@code level}'s dimension in place and writes back when changed.
     * @return true if NBT was updated
     */
    public static boolean pruneStack(ItemStack stack, Level level) {
        if (stack == null || stack.isEmpty() || level == null) {
            return false;
        }
        List<FoodPermitMark> marks = FoodPermitData.read(stack);
        if (marks.isEmpty()) {
            return false;
        }
        boolean[] valid = new boolean[marks.size()];
        for (int i = 0; i < marks.size(); i++) {
            FoodPermitMark mark = marks.get(i);
            if (!FoodPermitData.matchesDimension(mark, level)) {
                valid[i] = true;
                continue;
            }
            BlockPos pos = FoodPermitData.toPos(mark);
            BlockEntity be = level.getBlockEntity(pos);
            valid[i] = FoodPermitMarks.shouldKeepMark(
                    be != null && SnackRaidContainers.isMarkableContainer(be));
        }
        if (!FoodPermitMarks.removeInvalid(marks, valid)) {
            return false;
        }
        FoodPermitData.write(stack, marks);
        return true;
    }

    /** True when this mark should be drawn / targeted in {@code level}. */
    public static boolean isPresentMarkable(Level level, FoodPermitMark mark) {
        if (level == null || mark == null || !FoodPermitData.matchesDimension(mark, level)) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(FoodPermitData.toPos(mark));
        return FoodPermitMarks.shouldKeepMark(
                be != null && SnackRaidContainers.isMarkableContainer(be));
    }
}
