package com.touhoumaid.eatproperly.food;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FoodPool {
    private FoodPool() {
    }

    public static List<Item> collectAvailableFoods() {
        List<Item> foods = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS) {
            if (isPoolItem(item)) {
                foods.add(item);
            }
        }
        return foods;
    }

    /**
     * True when {@code item} belongs to the food-request pool: vanilla-edible and without
     * harmful food effects. Any mod's {@code FoodProperties} foods are included automatically.
     */
    public static boolean isPoolItem(Item item) {
        if (item == null || !item.isEdible()) {
            return false;
        }
        return !FoodPoolSafety.hasHarmfulFoodEffect(item.getFoodProperties());
    }

    /** True when {@code stack} belongs to the food-request pool. */
    public static boolean isPoolItem(ItemStack stack) {
        return !stack.isEmpty() && isPoolItem(stack.getItem());
    }

    /**
     * True when {@code stack} is in the maid's effective request pool:
     * global pool item, and if any Order Books are equipped, allowed by their intersection.
     */
    public static boolean isInEffectivePool(ItemStack stack, Collection<OrderBookSnapshot> books) {
        if (!isPoolItem(stack)) {
            return false;
        }
        String id = toId(stack);
        return !id.isEmpty() && OrderBookIntersection.allowsAll(books, id);
    }

    public static ItemStack pickRandom(RandomSource random) {
        return pickRandom(random, List.of());
    }

    /**
     * Picks from the global pool filtered by Order Books.
     * {@code filters} empty/null = no Order Book (full pool); multiple books = intersection.
     */
    public static ItemStack pickRandom(RandomSource random, Collection<OrderBookSnapshot> filters) {
        List<Item> foods = collectFilteredFoods(filters);
        if (foods.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(foods.get(random.nextInt(foods.size())));
    }

    /** @deprecated use {@link #pickRandom(RandomSource, Collection)} */
    public static ItemStack pickRandom(RandomSource random, OrderBookSnapshot filter) {
        return pickRandom(random, filter == null ? List.of() : List.of(filter));
    }

    /** Global pool entries that pass all Order Book filters (empty = unfiltered). */
    public static List<Item> collectFilteredFoods(Collection<OrderBookSnapshot> filters) {
        List<Item> foods = collectAvailableFoods();
        if (filters == null || filters.isEmpty()) {
            return foods;
        }
        List<Item> filtered = new ArrayList<>(foods.size());
        for (Item item : foods) {
            String id = toId(new ItemStack(item));
            if (!id.isEmpty() && OrderBookIntersection.allowsAll(filters, id)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    /** @deprecated use {@link #collectFilteredFoods(Collection)} */
    public static List<Item> collectFilteredFoods(OrderBookSnapshot filter) {
        return collectFilteredFoods(filter == null ? List.of() : List.of(filter));
    }

    public static ItemStack fromId(String id) {
        if (id == null || id.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !ForgeRegistries.ITEMS.containsKey(rl)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(ForgeRegistries.ITEMS.getValue(rl));
    }

    public static String toId(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }
}
