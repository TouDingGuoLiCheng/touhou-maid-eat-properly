package com.touhoumaid.eatproperly.recipe;

import com.touhoumaid.eatproperly.food.OrderBookData;
import com.touhoumaid.eatproperly.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Shapeless crafting: one configured Order Book + one {@link Items#BOOK} → duplicate with the same NBT.
 * The template Order Book is not consumed.
 */
public class OrderBookCopyRecipe extends CustomRecipe {
    public OrderBookCopyRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return OrderBookCopyMatch.findTemplate(container) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        ItemStack template = OrderBookCopyMatch.findTemplate(container);
        return template == null ? ItemStack.EMPTY : OrderBookData.copyStack(template);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.is(ModItems.ORDER_BOOK.get())) {
                remaining.set(slot, stack.copy());
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return new ItemStack(ModItems.ORDER_BOOK.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ORDER_BOOK_COPY.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }
}
