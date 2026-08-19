package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;

/**
 * Tracks world {@code gameTime} only while the maid is ticking so speech cooldowns
 * pause during chunk unload / while the owner is away. Also restaggers deferred
 * public chat after a gap.
 */
public final class MaidLoadedClock {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String LAST_SEEN = "LastSeenGameTime";
    private static final String NAG_ELAPSED = "NagElapsedLoadedTicks";
    private static final String HUNGER_ELAPSED = "HungerElapsedLoadedTicks";

    /** ~4 seconds; larger jumps mean the maid was unloaded (or the server stalled hard). */
    public static final long GAP_THRESHOLD_TICKS = 80L;
    /** Spread deferred public chats by ~30s of dayTime between entries. */
    public static final int DEFER_STAGGER_DAY_TICKS = 600;

    private MaidLoadedClock() {
    }

    private static CompoundTag root(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    /** Call once per maid tick-pass (safe if invoked from multiple handlers the same tick). */
    public static void sync(EntityMaid maid) {
        long now = maid.level().getGameTime();
        CompoundTag tag = root(maid);
        long lastSeen = tag.contains(LAST_SEEN) ? tag.getLong(LAST_SEEN) : -1L;
        boolean gap = LoadedTimePolicy.isUnloadGap(lastSeen, now, GAP_THRESHOLD_TICKS);
        long delta = lastSeen < 0L ? 0L : (gap ? 0L : now - lastSeen);

        if (gap) {
            MaidSpeech.reschedulePendingAfterGap(maid);
        }
        // Speech cooldowns pause while the owner is away (and while unloaded — delta is 0 on gap).
        boolean ownerNear = MaidSpeech.isOwnerNearby(maid);
        if (delta > 0L && ownerNear) {
            tag.putLong(NAG_ELAPSED, LoadedTimePolicy.advanceElapsed(tag.getLong(NAG_ELAPSED), delta, false));
            tag.putLong(HUNGER_ELAPSED, LoadedTimePolicy.advanceElapsed(tag.getLong(HUNGER_ELAPSED), delta, false));
        }
        tag.putLong(LAST_SEEN, now);
    }

    public static long getNagElapsed(EntityMaid maid) {
        return root(maid).getLong(NAG_ELAPSED);
    }

    public static void resetNagElapsed(EntityMaid maid) {
        root(maid).putLong(NAG_ELAPSED, 0L);
    }

    public static long getHungerElapsed(EntityMaid maid) {
        return root(maid).getLong(HUNGER_ELAPSED);
    }

    public static void resetHungerElapsed(EntityMaid maid) {
        root(maid).putLong(HUNGER_ELAPSED, 0L);
    }
}
