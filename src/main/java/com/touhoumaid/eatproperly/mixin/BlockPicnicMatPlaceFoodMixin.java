package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.block.BlockPicnicMat;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import com.touhoumaid.eatproperly.food.FoodRequestDeliveryEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockPicnicMat.class, remap = false)
public abstract class BlockPicnicMatPlaceFoodMixin {
    @Inject(method = "placeFood", at = @At("RETURN"))
    private static void eatproperly$completeRequestOnPlace(ItemStack stack, Player player, TileEntityPicnicMat mat,
                                                           CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.SUCCESS || player.level().isClientSide() || mat == null) {
            return;
        }
        FoodRequestDeliveryEvents.onPicnicMatFoodPlaced(player, mat.getHandler());
    }
}
