package com.touhoumaid.eatproperly.food;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class FoodPool {
    private FoodPool() {
    }

    public static List<Item> collectAvailableFoods() {
        List<Item> foods = new ArrayList<>();
        boolean fd = ModList.get().isLoaded(EatProperlyMod.FARMERS_DELIGHT_MOD_ID);
        boolean youkai = ModList.get().isLoaded(EatProperlyMod.YOUKAI_MOD_ID);

        for (Item item : ForgeRegistries.ITEMS) {
            if (!item.isEdible()) {
                continue;
            }
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null) {
                continue;
            }
            String ns = id.getNamespace();
            if ("minecraft".equals(ns)
                    || (fd && EatProperlyMod.FARMERS_DELIGHT_MOD_ID.equals(ns))
                    || (youkai && EatProperlyMod.YOUKAI_MOD_ID.equals(ns))) {
                foods.add(item);
            }
        }
        return foods;
    }

    public static ItemStack pickRandom(RandomSource random) {
        List<Item> foods = collectAvailableFoods();
        if (foods.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(foods.get(random.nextInt(foods.size())));
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
