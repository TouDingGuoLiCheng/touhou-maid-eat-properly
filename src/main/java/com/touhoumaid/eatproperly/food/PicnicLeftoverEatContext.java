package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Records which hand is finishing a real item use, so leftover routing only ever touches the
 * bowl/bottle the food turned into — never the gear the maid happens to be holding.
 *
 * <p>TLM's {@code EntityMaid#completeUsingItem} runs {@code super.completeUsingItem()} and then
 * {@code backCurrentHandItemStack()}, which unconditionally drains the OFF_HAND. Both leftover
 * hooks live inside that one synchronous server-side call, so a single static slot is enough.
 */
public final class PicnicLeftoverEatContext {
    private static EntityMaid eatingMaid;
    private static InteractionHand eatingHand;
    private static ItemStack eatenFood = ItemStack.EMPTY;

    private PicnicLeftoverEatContext() {
    }

    public static void begin(EntityMaid maid, InteractionHand hand, ItemStack food) {
        eatingMaid = maid;
        eatingHand = hand;
        eatenFood = food;
    }

    public static void end(EntityMaid maid) {
        if (eatingMaid != maid) {
            return;
        }
        eatingMaid = null;
        eatingHand = null;
        eatenFood = ItemStack.EMPTY;
    }

    public static InteractionHand eatingHand(EntityMaid maid) {
        return eatingMaid == maid ? eatingHand : null;
    }

    /**
     * Whether {@code stack} is what the food turned into after the maid ate from {@code hand}.
     * A partly eaten stack keeps the same item and is not a leftover.
     */
    public static boolean isLeftoverOf(EntityMaid maid, InteractionHand hand, ItemStack stack) {
        if (eatingMaid != maid || eatingHand != hand || eatenFood.isEmpty() || stack.isEmpty()) {
            return false;
        }
        return !ItemStack.isSameItem(stack, eatenFood);
    }
}
