package com.touhoumaid.eatproperly.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

/**
 * Inventory-backed lunchbox insert helpers (Forge handlers).
 * Prefer the maid's {@code getAvailableBackpackInv()} — not the full 36-slot {@code maidInv}.
 */
public final class LunchboxInvInsert {
    private LunchboxInvInsert() {
    }

    /**
     * Cumulative simulate: each stack occupies space for the next.
     */
    public static boolean canFullyInsertAll(IItemHandler dest, List<ItemStack> stacks) {
        if (dest == null || stacks == null || stacks.isEmpty()) {
            return false;
        }
        ItemStackHandler sim = copyHandler(dest);
        boolean any = false;
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            any = true;
            ItemStack leftover = ItemHandlerHelper.insertItemStacked(sim, stack.copy(), false);
            if (!leftover.isEmpty()) {
                return false;
            }
        }
        return any;
    }

    public static ItemStackHandler copyHandler(IItemHandler src) {
        ItemStackHandler copy = new ItemStackHandler(src.getSlots());
        for (int i = 0; i < src.getSlots(); i++) {
            copy.setStackInSlot(i, src.getStackInSlot(i).copy());
        }
        return copy;
    }
}
