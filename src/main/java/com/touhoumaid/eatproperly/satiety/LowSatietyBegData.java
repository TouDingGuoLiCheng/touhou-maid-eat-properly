package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;

/** N1 low-satiety begging flag + the food id that triggered the beg. */
public final class LowSatietyBegData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String ACTIVE = "LowSatietyBegging";
    private static final String BEGGED_FOOD = "LowSatietyBeggedFood";

    private LowSatietyBegData() {
    }

    private static CompoundTag root(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    public static boolean isActive(EntityMaid maid) {
        return root(maid).getBoolean(ACTIVE);
    }

    public static void setActive(EntityMaid maid, boolean active) {
        CompoundTag tag = root(maid);
        if (active) {
            tag.putBoolean(ACTIVE, true);
        } else {
            tag.remove(ACTIVE);
            tag.remove(BEGGED_FOOD);
        }
    }

    /** Item id of the owner's pool food that started this beg (may be empty). */
    public static String getBeggedFoodId(EntityMaid maid) {
        return root(maid).getString(BEGGED_FOOD);
    }

    public static void setBeggedFoodId(EntityMaid maid, String foodId) {
        if (foodId == null || foodId.isEmpty()) {
            root(maid).remove(BEGGED_FOOD);
        } else {
            root(maid).putString(BEGGED_FOOD, foodId);
        }
    }

    public static boolean isSameAsBeggedFood(EntityMaid maid, String givenFoodId) {
        String begged = getBeggedFoodId(maid);
        return !begged.isEmpty() && begged.equals(givenFoodId);
    }
}
