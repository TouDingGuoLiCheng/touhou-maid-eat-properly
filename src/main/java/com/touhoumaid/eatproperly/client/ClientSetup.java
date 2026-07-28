package com.touhoumaid.eatproperly.client;

import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.inventory.DualLunchboxContainer;
import com.touhoumaid.eatproperly.inventory.EatProperlyContainer;
import com.touhoumaid.eatproperly.inventory.LunchboxContainer;
import com.touhoumaid.eatproperly.inventory.TrinketContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(EatProperlyContainer.TYPE.get(), EatProperlyScreen::new);
            MenuScreens.register(LunchboxContainer.TYPE.get(), LunchboxScreen::new);
            MenuScreens.register(DualLunchboxContainer.TYPE.get(), DualLunchboxScreen::new);
            MenuScreens.register(TrinketContainer.TYPE.get(), TrinketScreen::new);
        });
    }
}
