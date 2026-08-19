package com.touhoumaid.eatproperly;

import com.mojang.logging.LogUtils;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.effect.ModEffects;
import com.touhoumaid.eatproperly.entity.ModEntities;
import com.touhoumaid.eatproperly.inventory.DualLunchboxContainer;
import com.touhoumaid.eatproperly.inventory.EatProperlyContainer;
import com.touhoumaid.eatproperly.inventory.LunchboxContainer;
import com.touhoumaid.eatproperly.inventory.OrderBookContainer;
import com.touhoumaid.eatproperly.inventory.TrinketContainer;
import com.touhoumaid.eatproperly.item.ModCreativeTabs;
import com.touhoumaid.eatproperly.item.ModItems;
import com.touhoumaid.eatproperly.recipe.ModRecipes;
import com.touhoumaid.eatproperly.sound.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(EatProperlyMod.MOD_ID)
public class EatProperlyMod {
    public static final String MOD_ID = "touhou_maid_eat_properly";
    public static final String TLM_MOD_ID = "touhou_little_maid";
    public static final String FARMERS_DELIGHT_MOD_ID = "farmersdelight";
    public static final String YOUKAI_MOD_ID = "youkaishomecoming";

    /** GUI tab icon: assets/.../textures/gui/green_tea.png */
    public static final String TAB_ICON = "textures/gui/green_tea.png";

    private static final Logger LOGGER = LogUtils.getLogger();

    public EatProperlyMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ModItems.ITEMS.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        EatProperlyContainer.MENUS.register(modEventBus);
        LunchboxContainer.MENUS.register(modEventBus);
        DualLunchboxContainer.MENUS.register(modEventBus);
        TrinketContainer.MENUS.register(modEventBus);
        OrderBookContainer.MENUS.register(modEventBus);
        context.registerConfig(ModConfig.Type.COMMON, EatProperlyConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(com.touhoumaid.eatproperly.network.ModNetwork::register);
        LOGGER.info("Touhou Maid: Eat properly loaded.");
        LOGGER.info("Touhou Little Maid present: {}", ModList.get().isLoaded(TLM_MOD_ID));
        LOGGER.info("Farmer's Delight present: {}", ModList.get().isLoaded(FARMERS_DELIGHT_MOD_ID));
        LOGGER.info("Youkai's Homecoming present: {}", ModList.get().isLoaded(YOUKAI_MOD_ID));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Touhou Maid: Eat properly server starting.");
    }
}
