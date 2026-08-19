package com.touhoumaid.eatproperly.compat;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.touhoumaid.eatproperly.item.ModItems;

/**
 * Registers milestone buff trinkets, the Order Book, and Food Permit as TLM maid baubles.
 * Night sparrow wrapper is intentionally omitted (player-only). Lore-only keepsakes are not bound.
 * Effects are applied by {@link com.touhoumaid.eatproperly.trinket.TrinketEffects}, not here.
 * Order Book filtering (intersection of all equipped books) is applied by
 * {@link com.touhoumaid.eatproperly.food.FoodRequestHandler}.
 */
@LittleMaidExtension
public class LittleMaidCompat implements ILittleMaid {
    private static final IMaidBauble PASSIVE = new IMaidBauble() {
    };

    @Override
    public void bindMaidBauble(BaubleManager manager) {
        manager.bind(ModItems.HEART_SEASONING.get(), PASSIVE);
        manager.bind(ModItems.INSULATED_MEAL_BAG.get(), PASSIVE);
        manager.bind(ModItems.MOON_APPLE_CANDY.get(), PASSIVE);
        manager.bind(ModItems.SCARLET_AMULET.get(), PASSIVE);
        manager.bind(ModItems.SWEET_SOFT_CANDY.get(), PASSIVE);
        manager.bind(ModItems.ONSEN_MANJU_BAG.get(), PASSIVE);
        manager.bind(ModItems.SOBERING_RAISIN.get(), PASSIVE);
        manager.bind(ModItems.LAKE_FAIRY_KEEPSAKE.get(), PASSIVE);
        manager.bind(ModItems.SILVER_BELL_CHARM.get(), PASSIVE);
        manager.bind(ModItems.ORDER_BOOK.get(), PASSIVE);
        manager.bind(ModItems.FOOD_PERMIT.get(), PASSIVE);
        // night_sparrow_wrapper: player-only — do not bind
    }
}
