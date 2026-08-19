package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidHomeMeal;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidWorkMeal;
import com.touhoumaid.eatproperly.food.EffectivePoolEatPolicy;
import com.touhoumaid.eatproperly.food.OutOfPoolDelivery;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * P1: work/home meals only accept food in the maid's effective request pool.
 */
@Mixin(value = {
        DefaultMaidWorkMeal.class,
        DefaultMaidHomeMeal.class
}, remap = false)
public abstract class MaidMealEffectivePoolMixin {
    @Inject(method = "canMaidEat", at = @At("RETURN"), cancellable = true)
    private void eatproperly$requireEffectivePool(
            EntityMaid maid, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || stack.isEmpty()) {
            return;
        }
        boolean inPool = OutOfPoolDelivery.isInEffectivePool(maid, stack);
        if (!EffectivePoolEatPolicy.allows(inPool)) {
            cir.setReturnValue(false);
        }
    }
}
