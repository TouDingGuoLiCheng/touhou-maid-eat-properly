package com.touhoumaid.eatproperly.recipe;

import com.touhoumaid.eatproperly.item.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Pure matching logic for {@link OrderBookCopyRecipe}. */
public final class OrderBookCopyMatch {
    private OrderBookCopyMatch() {
    }

    /**
     * @return the template Order Book when the grid is exactly one Order Book and one book; otherwise null
     */
    public static ItemStack findTemplate(Container container) {
        ItemStack template = ItemStack.EMPTY;
        boolean foundBook = false;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.ORDER_BOOK.get())) {
                if (!template.isEmpty()) {
                    return null;
                }
                template = stack;
            } else if (stack.is(Items.BOOK)) {
                if (foundBook) {
                    return null;
                }
                foundBook = true;
            } else {
                return null;
            }
        }
        return foundBook && !template.isEmpty() ? template : null;
    }
}
