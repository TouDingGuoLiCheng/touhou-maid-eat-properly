package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidHealSelfMeal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Heal-self meals only refill food + saturation; HP restores via {@link com.touhoumaid.eatproperly.satiety.MaidFoodData}.
 */
@Mixin(value = DefaultMaidHealSelfMeal.class, remap = false)
public abstract class DefaultMaidHealSelfMealSuppressHealMixin {
    @Redirect(
            method = "onMaidEat",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;m_5634_(F)V"
            )
    )
    private void eatproperly$suppressInstantHeal(EntityMaid maid, float amount) {
    }
}
