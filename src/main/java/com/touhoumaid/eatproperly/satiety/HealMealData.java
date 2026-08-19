package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;

/** Heal-self meal pacing stamp. */
public final class HealMealData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String LAST_HEAL_MEAL = "LastHealMealGameTime";

    private HealMealData() {
    }

    private static CompoundTag root(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    /** Negative when this maid has never eaten a heal-self meal. */
    public static long getLastHealMealTime(EntityMaid maid) {
        CompoundTag tag = root(maid);
        return tag.contains(LAST_HEAL_MEAL) ? tag.getLong(LAST_HEAL_MEAL) : -1L;
    }

    public static void setLastHealMealTime(EntityMaid maid, long gameTime) {
        root(maid).putLong(LAST_HEAL_MEAL, gameTime);
    }
}
