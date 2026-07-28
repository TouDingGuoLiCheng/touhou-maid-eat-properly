package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidHomeMeal;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidWorkMeal;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gate work / home meals by mod satiety. Heal-self meals are unaffected.
 * Vanilla TLM has no hunger gate, so maids would otherwise keep eating backpack food.
 */
@Mixin(value = {
        DefaultMaidWorkMeal.class,
        DefaultMaidHomeMeal.class
}, remap = false)
public abstract class MaidMealEatGateMixin {
    @Inject(method = "canMaidEat", at = @At("HEAD"), cancellable = true)
    private void eatproperly$requireLowSatiety(EntityMaid maid, ItemStack stack, InteractionHand hand,
                                               CallbackInfoReturnable<Boolean> cir) {
        SatietyData.ensureInitialized(maid);
        if (SatietyData.getSatiety(maid) >= EatProperlyConfig.EAT_WHEN_SATIETY_BELOW.get()) {
            cir.setReturnValue(false);
        }
    }
}
