package com.touhoumaid.eatproperly.client;

import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.entity.ModEntities;
import com.touhoumaid.eatproperly.inventory.DualLunchboxContainer;
import com.touhoumaid.eatproperly.inventory.EatProperlyContainer;
import com.touhoumaid.eatproperly.inventory.LunchboxContainer;
import com.touhoumaid.eatproperly.inventory.OrderBookContainer;
import com.touhoumaid.eatproperly.inventory.TrinketContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
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
            MenuScreens.register(OrderBookContainer.TYPE.get(), OrderBookScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WALL_HANG.get(), WallHangRenderer::new);
        event.registerEntityRenderer(ModEntities.FLOOR_DECOR.get(), FloorDecorRenderer::new);
    }

    /** Baked for wall-hang / floor-decor entity renderers (not referenced by flat inventory item models). */
    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(model("praise_certificate_3d"));
        event.register(model("banner_honest_boss_3d"));
        event.register(model("block_figurine_3d"));
    }

    private static ModelResourceLocation model(String path) {
        return new ModelResourceLocation(new ResourceLocation(EatProperlyMod.MOD_ID, path), "inventory");
    }
}
