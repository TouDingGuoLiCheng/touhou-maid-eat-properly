package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;

/** Maid persistent state for out-of-pool delivery accumulation and anger. */
public final class OutOfPoolAngerData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String WINDOW_END = "OutOfPoolWindowEnd";
    private static final String COUNT = "OutOfPoolCount";
    private static final String ANGER_END = "OutOfPoolAngerEnd";
    private static final String GUI_BUBBLE_AT = "OutOfPoolGuiBubbleAt";

    private OutOfPoolAngerData() {
    }

    private static CompoundTag root(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    public static OutOfPoolAngerPolicy.State read(EntityMaid maid) {
        CompoundTag tag = root(maid);
        return new OutOfPoolAngerPolicy.State(
                tag.getLong(WINDOW_END),
                tag.getInt(COUNT),
                tag.getLong(ANGER_END));
    }

    public static void write(EntityMaid maid, OutOfPoolAngerPolicy.State state) {
        CompoundTag tag = root(maid);
        if (state.windowEnd() <= 0L) {
            tag.remove(WINDOW_END);
        } else {
            tag.putLong(WINDOW_END, state.windowEnd());
        }
        if (state.count() <= 0) {
            tag.remove(COUNT);
        } else {
            tag.putInt(COUNT, state.count());
        }
        if (state.angerEnd() <= 0L) {
            tag.remove(ANGER_END);
        } else {
            tag.putLong(ANGER_END, state.angerEnd());
        }
    }

    public static long getGuiBubbleAt(EntityMaid maid) {
        return root(maid).getLong(GUI_BUBBLE_AT);
    }

    public static void setGuiBubbleAt(EntityMaid maid, long gameTime) {
        if (gameTime <= 0L) {
            root(maid).remove(GUI_BUBBLE_AT);
        } else {
            root(maid).putLong(GUI_BUBBLE_AT, gameTime);
        }
    }

    /** Expire window/anger lazily against {@code now}. */
    public static OutOfPoolAngerPolicy.State sync(EntityMaid maid, long now) {
        OutOfPoolAngerPolicy.State state = OutOfPoolAngerPolicy.expireAngerIfNeeded(now, read(maid));
        state = OutOfPoolAngerPolicy.expireWindowIfNeeded(now, state);
        write(maid, state);
        return state;
    }

    public static boolean isAngry(EntityMaid maid, long now) {
        return OutOfPoolAngerPolicy.isAngry(now, sync(maid, now).angerEnd());
    }
}
