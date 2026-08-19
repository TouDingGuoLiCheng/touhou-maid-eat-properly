package com.touhoumaid.eatproperly.food;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

import java.util.List;

/**
 * Filters food-request pool entries that apply harmful mob effects (poison, hunger, etc.).
 */
public final class FoodPoolSafety {
    private FoodPoolSafety() {
    }

    public static boolean isHarmfulEffectCategory(MobEffectCategory category) {
        return category != null && isHarmfulCategoryName(category.name());
    }

    /** Package-visible for JVM unit tests without Minecraft on the test compile classpath. */
    static boolean isHarmfulCategoryName(String categoryName) {
        return "HARMFUL".equals(categoryName);
    }

    /** True when {@code food} applies at least one harmful effect when eaten. */
    public static boolean hasHarmfulFoodEffect(FoodProperties food) {
        if (food == null) {
            return false;
        }
        return hasHarmfulFoodEffect(food.getEffects());
    }

    static boolean hasHarmfulFoodEffect(List<Pair<MobEffectInstance, Float>> effects) {
        if (effects == null || effects.isEmpty()) {
            return false;
        }
        for (Pair<MobEffectInstance, Float> entry : effects) {
            MobEffectInstance instance = entry.getFirst();
            if (instance == null) {
                continue;
            }
            MobEffect effect = instance.getEffect();
            if (effect != null && isHarmfulEffectCategory(effect.getCategory())) {
                return true;
            }
        }
        return false;
    }
}
