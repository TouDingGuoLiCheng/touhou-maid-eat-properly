package com.touhoumaid.eatproperly.item;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EatProperlyMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.touhou_maid_eat_properly"))
                    .icon(() -> new ItemStack(ModItems.LUNCHBOX.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.LUNCHBOX.get());
                        output.accept(ModItems.ORDER_BOOK.get());
                        output.accept(ModItems.FOOD_PERMIT.get());
                        output.accept(ModItems.EXTRA_MEAL_PERMIT.get());
                        output.accept(ModItems.HYPNOSIS_NOTE.get());
                        for (RegistryObject<Item> item : ModItems.REWARD_ITEMS) {
                            output.accept(item.get());
                        }
                    })
                    .build()
    );

    private ModCreativeTabs() {
    }
}
