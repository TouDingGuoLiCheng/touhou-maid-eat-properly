package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class SatietyHandler {
    private SatietyHandler() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof EntityMaid maid)) {
            return;
        }
        if (!maid.isAlive() || !maid.isTame()) {
            return;
        }
        if (maid.tickCount % 20 != 0) {
            return;
        }
        tickMaid(maid);
    }

    private static void tickMaid(EntityMaid maid) {
        SatietyData.ensureInitialized(maid);

        long day = SatietyData.currentGameDay(maid);
        long lastDay = SatietyData.getLastSatietyDay(maid);
        if (day > lastDay) {
            // Catch up if the maid was unloaded across multiple days.
            long steps = Math.min(day - lastDay, 30L);
            for (long i = 0; i < steps; i++) {
                applyDailySettlement(maid);
            }
            SatietyData.setLastSatietyDay(maid, day);
        }

        maybeHungerSpeech(maid);
    }

    private static void applyDailySettlement(EntityMaid maid) {
        int drain = SatietyData.dailyDrainAmount();
        if (drain > 0) {
            SatietyData.setSatiety(maid, SatietyData.getSatiety(maid) - drain);
        }

        int satiety = SatietyData.getSatiety(maid);
        int threshold = EatProperlyConfig.SATIETY_LOW_THRESHOLD.get();
        if (satiety >= threshold) {
            SatietyData.setLowSatietyDays(maid, 0);
            return;
        }

        int lowDays = SatietyData.getLowSatietyDays(maid) + 1;
        SatietyData.setLowSatietyDays(maid, lowDays);

        int grace = EatProperlyConfig.FAVOR_DECAY_GRACE_DAYS.get();
        if (lowDays > grace) {
            int decay = EatProperlyConfig.favorDecayPerDayExp();
            if (decay > 0) {
                FavorabilityManager manager = maid.getFavorabilityManager();
                manager.reduceWithoutLevel(decay);
            }
        }
    }

    private static void maybeHungerSpeech(EntityMaid maid) {
        if (SatietyData.getSatiety(maid) >= EatProperlyConfig.SATIETY_LOW_THRESHOLD.get()) {
            return;
        }
        long now = maid.level().getGameTime();
        long last = SatietyData.getLastHungerSpeechGameTime(maid);
        int interval = EatProperlyConfig.HUNGER_SPEECH_INTERVAL_TICKS.get();
        if (last >= 0 && now - last < interval) {
            return;
        }
        String text = MaidSpeech.formatHunger(maid, maid.getRandom());
        if (text.isEmpty()) {
            return;
        }
        MaidSpeech.speakToOwner(maid, text);
        SatietyData.setLastHungerSpeechGameTime(maid, now);
    }
}
