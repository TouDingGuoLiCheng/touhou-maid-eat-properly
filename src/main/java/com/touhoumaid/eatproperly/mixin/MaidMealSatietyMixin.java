package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidHealSelfMeal;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidHomeMeal;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidWorkMeal;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restore satiety when TLM meal tasks start eating (hand / backpack food).
 * Must run here rather than {@code MaidAfterEatEvent}: that event receives the post-shrink
 * stack, which is often empty and has no FoodProperties.
 */
@Mixin(value = {
        DefaultMaidHomeMeal.class,
        DefaultMaidWorkMeal.class,
        DefaultMaidHealSelfMeal.class
}, remap = false)
public abstract class MaidMealSatietyMixin {
    @Inject(method = "onMaidEat", at = @At("HEAD"))
    private void eatproperly$restoreSatiety(EntityMaid maid, ItemStack stack, InteractionHand hand, CallbackInfo ci) {
        if (maid.level().isClientSide() || stack.isEmpty()) {
            return;
        }
        FoodProperties food = stack.getFoodProperties(maid);
        if (food == null) {
            return;
        }
        int nutrition = food.getNutrition();
        float satMod = food.getSaturationModifier();
        if (nutrition > 0) {
            SatietyData.eatFood(maid, nutrition, satMod);
        }
    }
}
