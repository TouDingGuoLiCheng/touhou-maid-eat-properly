package com.touhoumaid.eatproperly.snackraid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.LivingEntity;

/**
 * Decides when TLM's owner-following behaviours must stand down for an ongoing snack raid.
 *
 * <p>Of all maid brain behaviours, only the two follow-owner tasks declare {@code WALK_TARGET} as
 * {@code REGISTERED}; every other walking task requires it to be absent and is therefore already
 * blocked while the raid holds the memory. Follow-owner uses {@code restrictRadius - 2} as its stop
 * distance (8 by default, so 6 blocks), which is well inside the raid search radius — without
 * yielding, the maid gets tugged between the chest and her owner every few ticks.
 */
public final class SnackRaidPriority {
    /** Follow-owner teleports the maid home from beyond {@code restrictRadius + 2}. */
    private static final double TELEPORT_MARGIN = 2.0D;

    private SnackRaidPriority() {
    }

    /**
     * True while a raid should outrank following the owner.
     *
     * <p>Only the nearby tug-of-war is suppressed. Past the follow task's teleport distance the task
     * keeps running, so an owner who walks off still gets the maid back instead of losing her.
     */
    public static boolean followShouldYield(EntityMaid maid) {
        if (!SnackRaidData.isActive(maid)) {
            return false;
        }
        LivingEntity owner = maid.getOwner();
        if (owner == null) {
            return false;
        }
        return maid.closerThan(owner, maid.getRestrictRadius() + TELEPORT_MARGIN);
    }
}
