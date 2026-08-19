package com.touhoumaid.eatproperly.recipe;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, EatProperlyMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<OrderBookCopyRecipe>> ORDER_BOOK_COPY =
            SERIALIZERS.register("order_book_copy", () -> new SimpleCraftingRecipeSerializer<>(OrderBookCopyRecipe::new));

    private ModRecipes() {
    }
}
