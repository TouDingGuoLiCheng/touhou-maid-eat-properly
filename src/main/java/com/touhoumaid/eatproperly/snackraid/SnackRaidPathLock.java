package com.touhoumaid.eatproperly.snackraid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

/**
 * Version-tolerant fallback that suppresses TLM's owner-following path without referencing its
 * task classes. The maid's original restriction is persisted and restored on every raid exit.
 */
public final class SnackRaidPathLock {
    private SnackRaidPathLock() {
    }

    public static void acquire(EntityMaid maid) {
        if (!SnackRaidData.isPathLocked(maid)) {
            SnackRaidData.savePathLock(
                    maid, maid.getRestrictCenter(), Math.round(maid.getRestrictRadius()));
        }
        int originalRadius = SnackRaidData.getOriginalRestrictRadius(maid);
        maid.restrictTo(
                SnackRaidData.getOriginalRestrictCenter(maid),
                SnackRaidPathLockPolicy.lockRadius(originalRadius));
    }

    /**
     * True once the owner is far enough that TLM would teleport the maid; the raid must then bow out
     * so that teleport can happen. An absent owner (offline or another dimension) is not "too far":
     * follow-owner cannot run without one either, so nothing is competing for the walk target.
     */
    public static boolean ownerTooFar(EntityMaid maid) {
        if (!SnackRaidData.isPathLocked(maid)) {
            return false;
        }
        LivingEntity owner = maid.getOwner();
        return owner != null && SnackRaidPathLockPolicy.ownerTooFar(
                maid.distanceTo(owner), SnackRaidData.getOriginalRestrictRadius(maid));
    }

    public static void release(EntityMaid maid) {
        if (!SnackRaidData.isPathLocked(maid)) {
            return;
        }
        BlockPos center = SnackRaidData.getOriginalRestrictCenter(maid);
        int radius = SnackRaidData.getOriginalRestrictRadius(maid);
        maid.restrictTo(center, radius);
        SnackRaidData.clearPathLock(maid);
    }
}
