package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

/** Approach / look-at owner while low-satiety begging (N1), matching TLM {@code MaidBegTask}. */
public final class LowSatietyBegPathing {
    private LowSatietyBegPathing() {
    }

    public static void approachOwner(EntityMaid maid, Player owner) {
        if (!maid.closerThan(owner, LowSatietyBegPolicy.CLOSE_ENOUGH)) {
            BehaviorUtils.setWalkAndLookTargetMemories(
                    maid, owner, LowSatietyBegPolicy.APPROACH_SPEED, LowSatietyBegPolicy.CLOSE_ENOUGH);
        } else {
            BehaviorUtils.lookAtEntity(maid, owner);
        }
    }

    public static void clearApproach(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getNavigation().stop();
    }
}
