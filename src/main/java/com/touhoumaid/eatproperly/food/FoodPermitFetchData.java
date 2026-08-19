package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;

/**
 * Persistent runtime data for Food Permit fetch cooldowns.
 */
public final class FoodPermitFetchData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String FOOD_PERMIT = "FoodPermitFetch";
    private static final String LAST_FETCH_TIME = "LastFetchTime";
    private static final String PENDING_PICNIC_REFILL = "PendingPicnicRefill";

    private FoodPermitFetchData() {
    }

    private static CompoundTag tag(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        CompoundTag root = persistent.getCompound(ROOT);
        if (!root.contains(FOOD_PERMIT)) {
            root.put(FOOD_PERMIT, new CompoundTag());
        }
        return root.getCompound(FOOD_PERMIT);
    }

    public static long getLastFetchTime(EntityMaid maid) {
        return tag(maid).getLong(LAST_FETCH_TIME);
    }

    public static void setLastFetchTime(EntityMaid maid, long gameTime) {
        tag(maid).putLong(LAST_FETCH_TIME, gameTime);
    }

    public static void clearCooldown(EntityMaid maid) {
        tag(maid).remove(LAST_FETCH_TIME);
    }

    public static boolean isPendingPicnicRefill(EntityMaid maid) {
        return tag(maid).getBoolean(PENDING_PICNIC_REFILL);
    }

    public static void setPendingPicnicRefill(EntityMaid maid, boolean pending) {
        if (pending) {
            tag(maid).putBoolean(PENDING_PICNIC_REFILL, true);
        } else {
            tag(maid).remove(PENDING_PICNIC_REFILL);
        }
    }
}
