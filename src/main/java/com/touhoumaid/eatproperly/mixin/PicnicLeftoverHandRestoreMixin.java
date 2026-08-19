package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.food.PicnicLeftoverEatContext;
import com.touhoumaid.eatproperly.food.PicnicLeftoverPlacement;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * A6.2 hands-full path: after picnic eat, the leftover bowl/dish often remains in the off-hand.
 * {@code EntityMaid.backCurrentHandItemStack} then tries the backpack and, if full, spawns an
 * {@code ItemEntity} directly — never going through {@code BowlFoodEvent}/{@code DefaultEatenEvent},
 * so {@link PicnicLeftoverContainerMixin} cannot see it. Intercept the backpack insert here.
 *
 * <p>That TLM method drains the off-hand whatever it holds, including gear the maid is carrying
 * when the meal ran in the main hand, so only a real leftover is diverted to nearby containers.
 */
@Mixin(value = EntityMaid.class, remap = false)
public abstract class PicnicLeftoverHandRestoreMixin {
    @Redirect(
            method = "backCurrentHandItemStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/items/ItemHandlerHelper;insertItemStacked(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack eatproperly$nearbyContainersBeforeHandRestore(
            IItemHandler inventory, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        EntityMaid maid = (EntityMaid) (Object) this;
        if (!simulate && PicnicLeftoverEatContext.isLeftoverOf(maid, InteractionHand.OFF_HAND, remaining)) {
            remaining = PicnicLeftoverPlacement.tryPlaceNearPicnicMat(maid, remaining);
            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return ItemHandlerHelper.insertItemStacked(inventory, remaining, simulate);
    }
}
