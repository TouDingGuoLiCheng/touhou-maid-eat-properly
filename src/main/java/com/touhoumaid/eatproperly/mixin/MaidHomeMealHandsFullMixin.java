package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidHomeMealTask;
import com.touhoumaid.eatproperly.satiety.HomeMealHandsPolicy;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * E1: allow picnic/home meals even when both hands are occupied and the held item
 * cannot simulate-stow into the backpack (vanilla shows {@code two_hand_is_full} and returns).
 */
@Mixin(value = MaidHomeMealTask.class, remap = false)
public abstract class MaidHomeMealHandsFullMixin {
    @Redirect(
            method = "start(Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/items/ItemHandlerHelper;insertItemStacked(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack eatproperly$allowHandsFull(
            IItemHandler inventory, ItemStack stack, boolean simulate) {
        ItemStack leftover = ItemHandlerHelper.insertItemStacked(inventory, stack, simulate);
        if (HomeMealHandsPolicy.shouldClearSimulateLeftover(simulate)
                && !leftover.isEmpty()
                && !HomeMealHandsPolicy.shouldAbortForHandFull(true)) {
            return ItemStack.EMPTY;
        }
        return leftover;
    }
}
