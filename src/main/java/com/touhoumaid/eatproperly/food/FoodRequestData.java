package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.debug.DebugOverrides;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class FoodRequestData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String ITEM = "RequestItem";
    /** Absolute gameTime when the request started (preferred). */
    private static final String START_TIME = "RequestStartGameTime";
    /** Legacy day index; migrated to {@link #START_TIME} on read. */
    private static final String START_DAY = "RequestStartDay";
    private static final String NEXT_DAY = "NextRequestDay";
    private static final String LAST_NAG = "LastNagGameTime";
    private static final String NEXT_NAG_INTERVAL = "NextNagIntervalTicks";
    private static final String INIT = "Initialized";
    /** Set by the hypnosis note: while true, any food-pool item (not just the exact request) completes the request. */
    private static final String FORGOTTEN = "RequestForgotten";

    public static final long TICKS_PER_DAY = 24000L;

    private FoodRequestData() {
    }

    private static CompoundTag root(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT, Tag.TAG_COMPOUND)) {
            CompoundTag created = new CompoundTag();
            persistent.put(ROOT, created);
            return created;
        }
        return persistent.getCompound(ROOT);
    }

    public static boolean hasRequest(EntityMaid maid) {
        return !getRequestItemId(maid).isEmpty();
    }

    public static String getRequestItemId(EntityMaid maid) {
        return root(maid).getString(ITEM);
    }

    public static ItemStack getRequestStack(EntityMaid maid) {
        return FoodPool.fromId(getRequestItemId(maid));
    }

    public static void setRequest(EntityMaid maid, ItemStack stack) {
        CompoundTag tag = root(maid);
        tag.putString(ITEM, FoodPool.toId(stack));
        long now = maid.level().getGameTime();
        tag.putLong(START_TIME, now);
        tag.putLong(START_DAY, now / TICKS_PER_DAY);
        tag.putLong(LAST_NAG, -1L);
        tag.putInt(NEXT_NAG_INTERVAL, 0);
        tag.putBoolean(FORGOTTEN, false);
    }

    public static void clearRequest(EntityMaid maid) {
        CompoundTag tag = root(maid);
        tag.putString(ITEM, "");
        tag.remove(START_TIME);
        tag.remove(START_DAY);
        tag.putLong(LAST_NAG, -1L);
        tag.putInt(NEXT_NAG_INTERVAL, 0);
        tag.putBoolean(FORGOTTEN, false);
    }

    /** Whether the maid currently "forgot" her request (hypnosis note): any food-pool item completes it. */
    public static boolean isForgotten(EntityMaid maid) {
        return root(maid).getBoolean(FORGOTTEN);
    }

    /** Only meaningful while {@link #hasRequest(EntityMaid)} is true; cleared automatically on set/clear. */
    public static void setForgotten(EntityMaid maid, boolean forgotten) {
        root(maid).putBoolean(FORGOTTEN, forgotten);
    }

    /**
     * Absolute gameTime when the current request started.
     * Migrates legacy day-index / missing tags so old requests do not instantly abandon.
     */
    public static long getRequestStartGameTime(EntityMaid maid) {
        CompoundTag tag = root(maid);
        if (tag.contains(START_TIME, Tag.TAG_ANY_NUMERIC)) {
            return tag.getLong(START_TIME);
        }
        long now = maid.level().getGameTime();
        if (tag.contains(START_DAY, Tag.TAG_ANY_NUMERIC)) {
            long start = tag.getLong(START_DAY) * TICKS_PER_DAY;
            tag.putLong(START_TIME, start);
            return start;
        }
        tag.putLong(START_TIME, now);
        tag.putLong(START_DAY, now / TICKS_PER_DAY);
        return now;
    }

    /** Game-day index of request start (for status display). */
    public static long getRequestStartDay(EntityMaid maid) {
        return getRequestStartGameTime(maid) / TICKS_PER_DAY;
    }

    /**
     * Backdate start so the request looks {@code daysAgo} game days old (debug).
     * Start time may be negative when the world is younger than {@code daysAgo} days —
     * otherwise age would clamp to world birth and abandonIn would stay wrong.
     */
    public static void setRequestAgeDays(EntityMaid maid, int daysAgo) {
        long now = maid.level().getGameTime();
        long start = now - (long) Math.max(0, daysAgo) * TICKS_PER_DAY;
        CompoundTag tag = root(maid);
        tag.putLong(START_TIME, start);
        tag.putLong(START_DAY, start / TICKS_PER_DAY);
    }

    public static long abandonDurationTicks() {
        return (long) EatProperlyConfig.REQUEST_ABANDON_DAYS.get() * TICKS_PER_DAY;
    }

    public static boolean shouldAbandon(EntityMaid maid) {
        if (!hasRequest(maid)) {
            return false;
        }
        long elapsed = maid.level().getGameTime() - getRequestStartGameTime(maid);
        return elapsed >= abandonDurationTicks();
    }

    /** Whole game days remaining until abandon (0 = due now / overdue). */
    public static long daysUntilAbandon(EntityMaid maid) {
        if (!hasRequest(maid)) {
            return -1L;
        }
        long remaining = abandonDurationTicks() - (maid.level().getGameTime() - getRequestStartGameTime(maid));
        if (remaining <= 0L) {
            return 0L;
        }
        return (remaining + TICKS_PER_DAY - 1L) / TICKS_PER_DAY;
    }

    public static long getNextRequestDay(EntityMaid maid) {
        return root(maid).getLong(NEXT_DAY);
    }

    public static void setNextRequestDay(EntityMaid maid, long day) {
        root(maid).putLong(NEXT_DAY, day);
    }

    public static long getLastNagGameTime(EntityMaid maid) {
        return root(maid).getLong(LAST_NAG);
    }

    public static void setLastNagGameTime(EntityMaid maid, long time) {
        root(maid).putLong(LAST_NAG, time);
    }

    public static int getNextNagIntervalTicks(EntityMaid maid) {
        return root(maid).getInt(NEXT_NAG_INTERVAL);
    }

    public static void setNextNagIntervalTicks(EntityMaid maid, int ticks) {
        root(maid).putInt(NEXT_NAG_INTERVAL, ticks);
    }

    public static int randomNagIntervalTicks(net.minecraft.util.RandomSource random) {
        int min = DebugOverrides.nagTicksMin().orElseGet(EatProperlyConfig.REQUEST_NAG_INTERVAL_TICKS_MIN::get);
        int max = DebugOverrides.nagTicksMax().orElseGet(EatProperlyConfig.REQUEST_NAG_INTERVAL_TICKS_MAX::get);
        if (max < min) {
            int tmp = min;
            min = max;
            max = tmp;
        }
        if (max == min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    public static boolean isInitialized(EntityMaid maid) {
        return root(maid).getBoolean(INIT);
    }

    public static void setInitialized(EntityMaid maid) {
        root(maid).putBoolean(INIT, true);
    }

    public static int randomIntervalDays(net.minecraft.util.RandomSource random) {
        int min = DebugOverrides.requestDaysMin().orElseGet(EatProperlyConfig.REQUEST_INTERVAL_DAYS_MIN::get);
        int max = DebugOverrides.requestDaysMax().orElseGet(EatProperlyConfig.REQUEST_INTERVAL_DAYS_MAX::get);
        if (max < min) {
            int tmp = min;
            min = max;
            max = tmp;
        }
        if (max == min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    public static long currentGameDay(EntityMaid maid) {
        return maid.level().getGameTime() / TICKS_PER_DAY;
    }
}
