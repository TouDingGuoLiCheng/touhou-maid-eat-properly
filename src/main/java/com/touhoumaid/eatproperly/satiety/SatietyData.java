package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.food.MaidWorldTime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/**
 * Extra satiety bookkeeping on the shared PersistentData root.
 * Current satiety value uses EntityMaid.getHunger/setHunger (synced).
 */
public final class SatietyData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String INIT = "SatietyInitialized";
    /** Schedule-calendar day (TLM midnight); replaces legacy {@code LastSatietyDay} (gameTime-based). */
    private static final String LAST_DAY = "LastSatietyScheduleDay";
    private static final String LEGACY_LAST_DAY = "LastSatietyDay";
    private static final String LOW_DAYS = "LowSatietyDays";
    private static final String LAST_SPEECH = "LastHungerSpeechGameTime";

    private SatietyData() {
    }

    private static CompoundTag root(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    public static boolean isInitialized(EntityMaid maid) {
        return root(maid).getBoolean(INIT);
    }

    public static void ensureInitialized(EntityMaid maid) {
        if (isInitialized(maid)) {
            return;
        }
        CompoundTag tag = root(maid);
        tag.putBoolean(INIT, true);
        tag.putLong(LAST_DAY, currentGameDay(maid));
        tag.remove(LEGACY_LAST_DAY);
        tag.putInt(LOW_DAYS, 0);
        tag.putLong(LAST_SPEECH, -1L);
        maid.setHunger(EatProperlyConfig.SATIETY_MAX);
        MaidFoodData.initializeDefaults(maid);
    }

    public static int getSatiety(EntityMaid maid) {
        return Mth.clamp(maid.getHunger(), 0, EatProperlyConfig.SATIETY_MAX);
    }

    public static void setSatiety(EntityMaid maid, int value) {
        maid.setHunger(Mth.clamp(value, 0, EatProperlyConfig.SATIETY_MAX));
        MaidFoodData.clampSaturationToFood(maid);
    }

    public static void addSatiety(EntityMaid maid, int nutrition) {
        MaidFoodData.eatNutritionOnly(maid, nutrition);
    }

    /** Restore food + saturation from a food item, mirroring player eating. */
    public static void eatFood(EntityMaid maid, int nutrition, float saturationModifier) {
        MaidFoodData.eat(maid, nutrition, saturationModifier);
    }

    public static long getLastSatietyDay(EntityMaid maid) {
        CompoundTag tag = root(maid);
        if (!tag.contains(LAST_DAY)) {
            // Migrate off gameTime-based LastSatietyDay: anchor to today (no back-drain).
            long today = currentGameDay(maid);
            tag.putLong(LAST_DAY, today);
            tag.remove(LEGACY_LAST_DAY);
            return today;
        }
        return tag.getLong(LAST_DAY);
    }

    public static void setLastSatietyDay(EntityMaid maid, long day) {
        CompoundTag tag = root(maid);
        tag.putLong(LAST_DAY, day);
        tag.remove(LEGACY_LAST_DAY);
    }

    public static int getLowSatietyDays(EntityMaid maid) {
        return root(maid).getInt(LOW_DAYS);
    }

    public static void setLowSatietyDays(EntityMaid maid, int days) {
        root(maid).putInt(LOW_DAYS, Math.max(0, days));
    }

    public static long getLastHungerSpeechGameTime(EntityMaid maid) {
        return root(maid).getLong(LAST_SPEECH);
    }

    public static void setLastHungerSpeechGameTime(EntityMaid maid, long time) {
        root(maid).putLong(LAST_SPEECH, time);
    }

    /** TLM schedule-calendar day (increments at GUI 24:00 / {@link MaidWorldTime#MIDNIGHT_TOD}). */
    public static long currentGameDay(EntityMaid maid) {
        return MaidWorldTime.scheduleDay(maid.level().getDayTime());
    }

    public static int dailyDrainAmount() {
        double ratio = EatProperlyConfig.SATIETY_DAILY_DRAIN_RATIO.get();
        return Mth.clamp((int) Math.round(EatProperlyConfig.SATIETY_MAX * ratio), 0, EatProperlyConfig.SATIETY_MAX);
    }
}
