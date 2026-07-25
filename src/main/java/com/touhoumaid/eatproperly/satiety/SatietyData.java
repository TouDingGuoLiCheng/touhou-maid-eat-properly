package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/**
 * Extra satiety bookkeeping on the shared PersistentData root.
 * Current satiety value uses EntityMaid.getHunger/setHunger (synced).
 */
public final class SatietyData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String INIT = "SatietyInitialized";
    private static final String LAST_DAY = "LastSatietyDay";
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
        tag.putInt(LOW_DAYS, 0);
        tag.putLong(LAST_SPEECH, -1L);
        maid.setHunger(EatProperlyConfig.SATIETY_MAX);
    }

    public static int getSatiety(EntityMaid maid) {
        return Mth.clamp(maid.getHunger(), 0, EatProperlyConfig.SATIETY_MAX);
    }

    public static void setSatiety(EntityMaid maid, int value) {
        maid.setHunger(Mth.clamp(value, 0, EatProperlyConfig.SATIETY_MAX));
    }

    public static void addSatiety(EntityMaid maid, int nutrition) {
        if (nutrition <= 0) {
            return;
        }
        ensureInitialized(maid);
        setSatiety(maid, getSatiety(maid) + nutrition);
        if (getSatiety(maid) >= EatProperlyConfig.SATIETY_LOW_THRESHOLD.get()) {
            setLowSatietyDays(maid, 0);
        }
    }

    public static long getLastSatietyDay(EntityMaid maid) {
        return root(maid).getLong(LAST_DAY);
    }

    public static void setLastSatietyDay(EntityMaid maid, long day) {
        root(maid).putLong(LAST_DAY, day);
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

    public static long currentGameDay(EntityMaid maid) {
        return maid.level().getGameTime() / 24000L;
    }

    public static int dailyDrainAmount() {
        double ratio = EatProperlyConfig.SATIETY_DAILY_DRAIN_RATIO.get();
        return Mth.clamp((int) Math.round(EatProperlyConfig.SATIETY_MAX * ratio), 0, EatProperlyConfig.SATIETY_MAX);
    }
}
