package com.touhoumaid.eatproperly.compat;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.touhoumaid.eatproperly.item.ModItems;

/**
 * Registers milestone buff trinkets as TLM maid baubles so they can be worn on the maid bauble page.
 * Night sparrow wrapper is intentionally omitted (player-only). Lore-only keepsakes are not bound.
 * Effects are applied by {@link com.touhoumaid.eatproperly.trinket.TrinketEffects}, not here.
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
        // night_sparrow_wrapper: player-only — do not bind
    }
}
