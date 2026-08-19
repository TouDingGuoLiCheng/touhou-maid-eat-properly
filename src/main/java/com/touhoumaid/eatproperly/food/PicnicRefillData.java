package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;

/**
 * Runtime flag for the "left the mat to fetch picnic refill" intent (N3.10).
 * While armed without carried food, home-meal re-seating is suppressed so the maid can leave.
 * Remembers the original picnic mat so she returns there after fetch.
 */
public final class PicnicRefillData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String NODE = "PicnicRefill";
    private static final String SEEK_SINCE = "SeekSince";
    private static final String HOME_MAT = "HomeMat";
    private static final String HOME_DIM = "HomeDim";

    private PicnicRefillData() {
    }

    private static CompoundTag tag(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        CompoundTag root = persistent.getCompound(ROOT);
        if (!root.contains(NODE)) {
            root.put(NODE, new CompoundTag());
        }
        return root.getCompound(NODE);
    }

    public static boolean isSeeking(EntityMaid maid) {
        return tag(maid).contains(SEEK_SINCE);
    }

    public static long getSeekSince(EntityMaid maid) {
        return tag(maid).getLong(SEEK_SINCE);
    }

    public static void setSeeking(EntityMaid maid, long gameTime) {
        tag(maid).putLong(SEEK_SINCE, gameTime);
    }

    public static void setSeeking(EntityMaid maid, long gameTime, BlockPos homeMat, ResourceLocation dimension) {
        CompoundTag t = tag(maid);
        t.putLong(SEEK_SINCE, gameTime);
        if (homeMat != null) {
            t.putLong(HOME_MAT, homeMat.asLong());
        } else {
            t.remove(HOME_MAT);
        }
        if (dimension != null) {
            t.putString(HOME_DIM, dimension.toString());
        } else {
            t.remove(HOME_DIM);
        }
    }

    public static boolean hasHomeMat(EntityMaid maid) {
        return tag(maid).contains(HOME_MAT);
    }

    public static BlockPos getHomeMatPos(EntityMaid maid) {
        CompoundTag t = tag(maid);
        if (!t.contains(HOME_MAT)) {
            return null;
        }
        return BlockPos.of(t.getLong(HOME_MAT));
    }

    public static String getHomeMatDimension(EntityMaid maid) {
        CompoundTag t = tag(maid);
        return t.contains(HOME_DIM) ? t.getString(HOME_DIM) : "";
    }

    public static boolean matchesHomeMat(EntityMaid maid, Level level, BlockPos matPos) {
        if (matPos == null || !hasHomeMat(maid)) {
            return false;
        }
        BlockPos home = getHomeMatPos(maid);
        if (home == null || !home.equals(matPos)) {
            return false;
        }
        String dim = getHomeMatDimension(maid);
        if (dim.isEmpty() || level == null) {
            return true;
        }
        return dim.equals(level.dimension().location().toString());
    }

    /** True when seeking remembers a mat that is no longer a picnic mat in this dimension. */
    public static boolean isHomeMatMissing(EntityMaid maid, ServerLevel level) {
        if (!isSeeking(maid) || !hasHomeMat(maid)) {
            return false;
        }
        String dim = getHomeMatDimension(maid);
        if (!dim.isEmpty() && !dim.equals(level.dimension().location().toString())) {
            return false;
        }
        BlockPos home = getHomeMatPos(maid);
        if (home == null) {
            return true;
        }
        BlockEntity be = level.getBlockEntity(home);
        return !(be instanceof TileEntityPicnicMat);
    }

    public static void clearSeeking(EntityMaid maid) {
        CompoundTag t = tag(maid);
        t.remove(SEEK_SINCE);
        t.remove(HOME_MAT);
        t.remove(HOME_DIM);
    }
}
