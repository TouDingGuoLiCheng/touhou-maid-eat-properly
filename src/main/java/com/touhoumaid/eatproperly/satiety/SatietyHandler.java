package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.food.MaidLoadedClock;
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
        MaidFoodData.tick(maid);
        if (maid.tickCount % 20 != 0) {
            return;
        }
        tickMaid(maid);
    }

    private static void tickMaid(EntityMaid maid) {
        MaidLoadedClock.sync(maid);
        SatietyData.ensureInitialized(maid);

        long day = SatietyData.currentGameDay(maid);
        long lastDay = SatietyData.getLastSatietyDay(maid);
        if (day > lastDay) {
            // Settle once per TLM schedule midnight (24:00) while the maid is ticking.
            // Do not catch up satiety / favor for days spent unloaded outside loaded chunks.
            if (day == lastDay + 1) {
                applyDailySettlement(maid);
            }
            SatietyData.setLastSatietyDay(maid, day);
        }

        IdleWorkMealHandler.tick(maid);
        maybeHungerSpeech(maid);
    }

    private static void applyDailySettlement(EntityMaid maid) {
        String taskUid = maid.getTask() == null ? null : String.valueOf(maid.getTask().getUid());
        if (MaidSatietyDrainPolicy.shouldApplyDailyDrain(MaidSatietyDrainPolicy.isIdleTaskUid(taskUid))) {
            int drain = SatietyData.dailyDrainAmount();
            if (drain > 0) {
                double mult = com.touhoumaid.eatproperly.trinket.TrinketEffects.insulatedMealBagDrainMultiplier(maid);
                int reduced = (int) Math.round(drain * mult);
                if (reduced > 0) {
                    MaidFoodData.drainSaturationFirst(maid, reduced);
                }
            }
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
                com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager manager =
                        maid.getFavorabilityManager();
                manager.reduceWithoutLevel(decay);
            }
        }
    }

    private static void maybeHungerSpeech(EntityMaid maid) {
        if (SatietyData.getSatiety(maid) >= EatProperlyConfig.SATIETY_LOW_THRESHOLD.get()) {
            return;
        }
        int interval = EatProperlyConfig.HUNGER_SPEECH_INTERVAL_TICKS.get();
        if (MaidLoadedClock.getHungerElapsed(maid) < interval) {
            return;
        }

        boolean hasFood = HungerBackpackMeal.hasEdibleFood(maid);
        if (HungerSpeechEatFirstPolicy.decide(hasFood) == HungerSpeechEatFirstPolicy.Decision.EAT_FIRST) {
            boolean ate = HungerBackpackMeal.tryEatOne(maid);
            if (HungerSpeechEatFirstPolicy.shouldResetHungerElapsedAfterEat(ate)) {
                MaidLoadedClock.resetHungerElapsed(maid);
                return;
            }
            if (!HungerSpeechEatFirstPolicy.shouldSpeakAfterFailedEat(ate)) {
                return;
            }
            // Fall through: could not eat despite pool food (cooldown / hands full / idle gap).
        }

        String text = MaidSpeech.formatHunger(maid, maid.getRandom());
        if (text.isEmpty()) {
            return;
        }
        if (!MaidSpeech.bubbleToOwnerIfNear(maid, text)) {
            return;
        }
        MaidLoadedClock.resetHungerElapsed(maid);
        SatietyData.setLastHungerSpeechGameTime(maid, maid.level().getGameTime());
    }
}
