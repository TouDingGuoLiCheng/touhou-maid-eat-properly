package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.edible.CakeEdible;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Placed cake is an edible block, not an Item with FoodProperties.
 * Vanilla cake bites restore 2 hunger; mirror that into our satiety when the maid takes a bite.
 */
@Mixin(value = CakeEdible.class, remap = false)
public abstract class CakeEdibleMixin {
    /** Same nutrition as {@code CakeBlock} player bite. */
    private static final int CAKE_BITE_NUTRITION = 2;

    @Inject(method = "consume", at = @At("RETURN"))
    private void eatproperly$restoreSatietyFromCake(EntityMaid maid, BlockPos pos, BlockState state,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (maid.level().isClientSide() || !cir.getReturnValueZ()) {
            return;
        }
        SatietyData.eatFood(maid, CAKE_BITE_NUTRITION, 0.2F);
    }
}
