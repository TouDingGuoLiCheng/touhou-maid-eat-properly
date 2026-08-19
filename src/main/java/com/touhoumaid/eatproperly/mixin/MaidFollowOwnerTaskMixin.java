package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.satiety.LowSatietyBegData;
import com.touhoumaid.eatproperly.snackraid.SnackRaidPriority;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps the follow-owner task from overwriting snack-raid or low-satiety-beg walk targets.
 */
@Pseudo
@Mixin(targets = "com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidFollowOwnerTask",
        remap = false)
public abstract class MaidFollowOwnerTaskMixin {
    @Inject(
            method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;"
                    + "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)Z",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void eatproperly$yieldToSnackRaidOrBeg(ServerLevel level, EntityMaid maid, CallbackInfoReturnable<Boolean> cir) {
        if (SnackRaidPriority.followShouldYield(maid) || LowSatietyBegData.isActive(maid)) {
            cir.setReturnValue(false);
        }
    }
}
