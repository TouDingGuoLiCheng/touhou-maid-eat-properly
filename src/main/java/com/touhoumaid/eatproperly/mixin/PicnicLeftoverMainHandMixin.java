package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.food.PicnicLeftoverEatContext;
import com.touhoumaid.eatproperly.food.PicnicLeftoverPlacement;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A6.2b: empty-hands picnic eats happen in MAIN_HAND, and TLM's
 * {@code backCurrentHandItemStack} only drains OFF_HAND, so the bowl stays in the main hand.
 *
 * <p>The head hook records the eat for {@link PicnicLeftoverHandRestoreMixin} as well; both hooks
 * act only on the container the food turned into, so gear the maid carries is left alone.
 */
@Mixin(value = EntityMaid.class, remap = false)
public abstract class PicnicLeftoverMainHandMixin {
    @Inject(method = "m_8095_", at = @At("HEAD"))
    private void eatproperly$markEatingHand(CallbackInfo ci) {
        EntityMaid maid = (EntityMaid) (Object) this;
        if (maid.level() == null || maid.level().isClientSide()) {
            return;
        }
        InteractionHand hand = maid.getUsedItemHand();
        ItemStack using = maid.getUseItem();
        // Mirrors LivingEntity#completeUsingItem: any other state takes the releaseUsingItem branch,
        // which finishes no food and must not be mistaken for a meal.
        if (!maid.isUsingItem() || using.isEmpty() || using != maid.getItemInHand(hand)) {
            return;
        }
        PicnicLeftoverEatContext.begin(maid, hand, using.copy());
    }

    @Inject(method = "m_8095_", at = @At("TAIL"))
    private void eatproperly$placeMainHandLeftoverAfterEat(CallbackInfo ci) {
        EntityMaid maid = (EntityMaid) (Object) this;
        if (maid.level() == null || maid.level().isClientSide()) {
            return;
        }
        ItemStack main = maid.getMainHandItem();
        boolean leftover = PicnicLeftoverEatContext.isLeftoverOf(maid, InteractionHand.MAIN_HAND, main);
        PicnicLeftoverEatContext.end(maid);
        if (!leftover) {
            return;
        }

        ItemStack remaining = PicnicLeftoverPlacement.tryPlaceNearPicnicMat(maid, main.copy());
        if (remaining.isEmpty()) {
            maid.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return;
        }
        if (remaining.getCount() >= main.getCount()) {
            return;
        }
        ItemStack afterBackpack = ItemHandlerHelper.insertItemStacked(
                maid.getAvailableBackpackInv(), remaining, false);
        maid.setItemInHand(InteractionHand.MAIN_HAND, afterBackpack.isEmpty() ? ItemStack.EMPTY : afterBackpack);
    }
}
