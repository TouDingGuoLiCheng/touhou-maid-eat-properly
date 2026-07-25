package com.touhoumaid.eatproperly.config;

import com.mojang.logging.LogUtils;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.milestone.MilestoneRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ConfigReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ConfigReloadListener() {
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        if (!isOurs(event)) {
            return;
        }
        logSnapshot("loaded");
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        if (!isOurs(event)) {
            return;
        }
        logSnapshot("reloaded");
    }

    private static boolean isOurs(ModConfigEvent event) {
        return event.getConfig().getModId().equals(EatProperlyMod.MOD_ID);
    }

    private static void logSnapshot(String phase) {
        LOGGER.info(
                "Eat properly config {}: maxFavorLevel={}, expPerLevel={}, requestDays={}-{}, nagTicks={}-{}, drain={}, graceDays={}, milestones={}",
                phase,
                EatProperlyConfig.MAX_FAVOR_LEVEL.get(),
                EatProperlyConfig.EXP_PER_LEVEL.get(),
                EatProperlyConfig.REQUEST_INTERVAL_DAYS_MIN.get(),
                EatProperlyConfig.REQUEST_INTERVAL_DAYS_MAX.get(),
                EatProperlyConfig.REQUEST_NAG_INTERVAL_TICKS_MIN.get(),
                EatProperlyConfig.REQUEST_NAG_INTERVAL_TICKS_MAX.get(),
                EatProperlyConfig.SATIETY_DAILY_DRAIN_RATIO.get(),
                EatProperlyConfig.FAVOR_DECAY_GRACE_DAYS.get(),
                MilestoneRegistry.parseConfigured().size()
        );
    }
}
