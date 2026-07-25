package com.touhoumaid.eatproperly.milestone;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidFavorabilityLevelChangeEvent;
import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class MilestoneEvents {
    private MilestoneEvents() {
    }

    @SubscribeEvent
    public static void onFavorLevelChange(MaidFavorabilityLevelChangeEvent event) {
        MilestoneRegistry.onFavorLevelChange(event.getMaid(), event.getOldLevel(), event.getNewLevel());
    }
}
