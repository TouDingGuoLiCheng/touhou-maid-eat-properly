package com.touhoumaid.eatproperly.item;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EatProperlyMod.MOD_ID);

    public static final RegistryObject<Item> LUNCHBOX = ITEMS.register(
            "lunchbox",
            () -> new LunchboxItem(new Item.Properties().stacksTo(1))
    );

    private ModItems() {
    }
}
