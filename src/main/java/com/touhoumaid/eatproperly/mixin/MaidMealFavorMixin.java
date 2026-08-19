package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidHomeMeal;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidWorkMeal;
import com.touhoumaid.eatproperly.satiety.MealFavorPolicy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Work/home meal eats grant {@code floor(nutrition × 1.5)} favor instead of TLM's 0–1 roll.
 * Fake-skip ({@code apply(type, 0)} from meal tasks) does not go through {@code onMaidEat}.
 */
@Mixin(value = {
        DefaultMaidWorkMeal.class,
        DefaultMaidHomeMeal.class
}, remap = false)
public abstract class MaidMealFavorMixin {
    @Redirect(
            method = "onMaidEat",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/favorability/FavorabilityManager;apply(Lcom/github/tartaricacid/touhoulittlemaid/entity/favorability/Type;I)V"
            )
    )
    private void eatproperly$applyNutritionMealFavor(
            FavorabilityManager manager,
            Type type,
            int originalPoints,
            EntityMaid maid,
            ItemStack stack,
            InteractionHand hand) {
        FoodProperties food = stack.getFoodProperties(maid);
        int nutrition = food == null ? 0 : food.getNutrition();
        manager.apply(type, MealFavorPolicy.pointsForNutrition(nutrition));
    }
}
