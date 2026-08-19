package com.touhoumaid.eatproperly.snackraid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * Snack-raid state on the shared {@code TouhouMaidEatProperly} persistent root.
 */
public final class SnackRaidData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String RAID = "SnackRaid";

    private static final String PHASE = "Phase";
    private static final String CHEST = "Chest";
    private static final String STAND = "Stand";
    private static final String FLEE = "Flee";
    private static final String PHASE_TICKS = "PhaseTicks";
    private static final String LAST_RAID_TIME = "LastRaidTime";
    private static final String RETRY_AFTER = "RetryAfter";
    private static final String CACHED_CHEST = "CachedChest";
    private static final String LOCAL_ALLOWED = "LocalAllowed";
    private static final String FOOD_PERMIT_MODE = "FoodPermitMode";
    private static final String FOOD_PERMIT_PICNIC = "FoodPermitPicnic";
    private static final String PATH_LOCKED = "PathLocked";
    private static final String ORIGINAL_RESTRICT_CENTER = "OriginalRestrictCenter";
    private static final String ORIGINAL_RESTRICT_RADIUS = "OriginalRestrictRadius";

    private SnackRaidData() {
    }

    private static CompoundTag raid(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        CompoundTag root = persistent.getCompound(ROOT);
        if (!root.contains(RAID)) {
            root.put(RAID, new CompoundTag());
        }
        return root.getCompound(RAID);
    }

    public static SnackRaidPhase getPhase(EntityMaid maid) {
        return SnackRaidPhase.fromId(raid(maid).getInt(PHASE));
    }

    public static void setPhase(EntityMaid maid, SnackRaidPhase phase) {
        raid(maid).putInt(PHASE, phase.id());
    }

    public static boolean isActive(EntityMaid maid) {
        return getPhase(maid) != SnackRaidPhase.IDLE;
    }

    public static BlockPos getChestPos(EntityMaid maid) {
        return readPos(raid(maid), CHEST);
    }

    public static void setChestPos(EntityMaid maid, BlockPos pos) {
        writePos(raid(maid), CHEST, pos);
    }

    public static BlockPos getStandPos(EntityMaid maid) {
        return readPos(raid(maid), STAND);
    }

    public static void setStandPos(EntityMaid maid, BlockPos pos) {
        writePos(raid(maid), STAND, pos);
    }

    public static BlockPos getFleePos(EntityMaid maid) {
        return readPos(raid(maid), FLEE);
    }

    public static void setFleePos(EntityMaid maid, BlockPos pos) {
        writePos(raid(maid), FLEE, pos);
    }

    public static int getPhaseTicks(EntityMaid maid) {
        return raid(maid).getInt(PHASE_TICKS);
    }

    public static void setPhaseTicks(EntityMaid maid, int ticks) {
        raid(maid).putInt(PHASE_TICKS, Math.max(0, ticks));
    }

    public static long getLastRaidTime(EntityMaid maid) {
        return raid(maid).getLong(LAST_RAID_TIME);
    }

    public static void setLastRaidTime(EntityMaid maid, long gameTime) {
        raid(maid).putLong(LAST_RAID_TIME, gameTime);
    }

    /** Game time before which no new raid may start, used as a breather after a failed attempt. */
    public static long getRetryAfter(EntityMaid maid) {
        return raid(maid).getLong(RETRY_AFTER);
    }

    public static void setRetryAfter(EntityMaid maid, long gameTime) {
        raid(maid).putLong(RETRY_AFTER, gameTime);
    }

    /** Drops both the post-success cooldown and the post-failure breather. */
    public static void clearCooldowns(EntityMaid maid) {
        CompoundTag tag = raid(maid);
        tag.remove(LAST_RAID_TIME);
        tag.remove(RETRY_AFTER);
    }

    static boolean isPathLocked(EntityMaid maid) {
        return raid(maid).getBoolean(PATH_LOCKED);
    }

    static void savePathLock(EntityMaid maid, BlockPos center, int radius) {
        CompoundTag tag = raid(maid);
        tag.putBoolean(PATH_LOCKED, true);
        tag.putLong(ORIGINAL_RESTRICT_CENTER, center.asLong());
        tag.putInt(ORIGINAL_RESTRICT_RADIUS, radius);
    }

    static BlockPos getOriginalRestrictCenter(EntityMaid maid) {
        return BlockPos.of(raid(maid).getLong(ORIGINAL_RESTRICT_CENTER));
    }

    static int getOriginalRestrictRadius(EntityMaid maid) {
        return raid(maid).getInt(ORIGINAL_RESTRICT_RADIUS);
    }

    static void clearPathLock(EntityMaid maid) {
        CompoundTag tag = raid(maid);
        tag.remove(PATH_LOCKED);
        tag.remove(ORIGINAL_RESTRICT_CENTER);
        tag.remove(ORIGINAL_RESTRICT_RADIUS);
    }

    public static BlockPos getCachedChestPos(EntityMaid maid) {
        return readPos(raid(maid), CACHED_CHEST);
    }

    public static void setCachedChestPos(EntityMaid maid, BlockPos pos) {
        writePos(raid(maid), CACHED_CHEST, pos);
    }

    public static void clearCachedChestPos(EntityMaid maid) {
        raid(maid).remove(CACHED_CHEST);
    }

    public static boolean isFoodPermitMode(EntityMaid maid) {
        return raid(maid).getBoolean(FOOD_PERMIT_MODE);
    }

    public static void setFoodPermitMode(EntityMaid maid, boolean enabled) {
        raid(maid).putBoolean(FOOD_PERMIT_MODE, enabled);
    }

    public static boolean isFoodPermitPicnicMode(EntityMaid maid) {
        return raid(maid).getBoolean(FOOD_PERMIT_PICNIC);
    }

    public static void setFoodPermitPicnicMode(EntityMaid maid, boolean enabled) {
        raid(maid).putBoolean(FOOD_PERMIT_PICNIC, enabled);
    }

    public static boolean isAllowedLocally(EntityMaid maid) {
        CompoundTag tag = raid(maid);
        if (!tag.contains(LOCAL_ALLOWED)) {
            return SnackRaidToggleTexts.defaultAllowed();
        }
        return tag.getBoolean(LOCAL_ALLOWED);
    }

    public static void setAllowedLocally(EntityMaid maid, boolean allowed) {
        raid(maid).putBoolean(LOCAL_ALLOWED, allowed);
    }

    public static boolean toggleAllowedLocally(EntityMaid maid) {
        boolean next = !isAllowedLocally(maid);
        setAllowedLocally(maid, next);
        return next;
    }

    public static void clearActive(EntityMaid maid) {
        CompoundTag tag = raid(maid);
        tag.putInt(PHASE, SnackRaidPhase.IDLE.id());
        tag.remove(CHEST);
        tag.remove(STAND);
        tag.remove(FLEE);
        tag.remove(FOOD_PERMIT_MODE);
        tag.remove(FOOD_PERMIT_PICNIC);
        tag.putInt(PHASE_TICKS, 0);
    }

    private static BlockPos readPos(CompoundTag tag, String key) {
        if (!tag.contains(key)) {
            return BlockPos.ZERO;
        }
        return BlockPos.of(tag.getLong(key));
    }

    private static void writePos(CompoundTag tag, String key, BlockPos pos) {
        if (pos == null || pos.equals(BlockPos.ZERO)) {
            tag.remove(key);
            return;
        }
        tag.putLong(key, pos.asLong());
    }
}
