package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAfterEatEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.event.food.BowlFoodEvent;
import com.github.tartaricacid.touhoulittlemaid.event.food.DefaultEatenEvent;
import com.touhoumaid.eatproperly.food.PicnicLeftoverPlacement;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * E2 / A6.2: before TLM inserts leftovers from BowlFoodEvent / DefaultEatenEvent into the
 * maid inventory (or drops them), try nearby containers around the picnic mat.
 * Hands-full off-hand restore is handled separately by {@link PicnicLeftoverHandRestoreMixin}.
 * Empty-hands main-hand leftovers are handled by {@link PicnicLeftoverMainHandMixin}.
 */
@Mixin(value = {
        DefaultEatenEvent.class,
        BowlFoodEvent.class
}, remap = false)
public abstract class PicnicLeftoverContainerMixin {
    @Redirect(
            method = "onAfterMaidEat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/items/ItemHandlerHelper;insertItemStacked(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack eatproperly$nearbyContainersFirst(
            IItemHandler inventory, ItemStack stack, boolean simulate, MaidAfterEatEvent event) {
        ItemStack remaining = stack;
        if (!simulate && !remaining.isEmpty()) {
            EntityMaid maid = event.getMaid();
            remaining = PicnicLeftoverPlacement.tryPlaceNearPicnicMat(maid, remaining);
            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return ItemHandlerHelper.insertItemStacked(inventory, remaining, simulate);
    }
}
