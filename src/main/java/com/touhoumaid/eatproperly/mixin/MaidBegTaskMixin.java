package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.food.AngerAttractionPolicy;
import com.touhoumaid.eatproperly.food.OutOfPoolAngerData;
import com.touhoumaid.eatproperly.satiety.LowSatietyBegData;
import com.touhoumaid.eatproperly.snackraid.SnackRaidBegPolicy;
import com.touhoumaid.eatproperly.snackraid.SnackRaidData;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keep low-satiety beg animation; block TLM cake/food beg while out-of-pool angry (P2) or while
 * a snack raid / permit fetch is running (N3.7-nod).
 */
@Pseudo
@Mixin(targets = "com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidBegTask",
        remap = false)
public abstract class MaidBegTaskMixin {
    @Inject(
            method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;"
                    + "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void eatproperly$blockBegWhenBusy(
            ServerLevel level, EntityMaid maid, CallbackInfoReturnable<Boolean> cir) {
        if (!eatproperly$allowBeg(maid)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "stop(Lnet/minecraft/server/level/ServerLevel;"
                    + "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;J)V",
            at = @At("RETURN"),
            remap = false,
            require = 0
    )
    private void eatproperly$keepLowSatietyBegging(ServerLevel level, EntityMaid maid, long gameTime, CallbackInfo ci) {
        if (LowSatietyBegData.isActive(maid) && eatproperly$allowBeg(maid)) {
            maid.setBegging(true);
        }
    }

    private static boolean eatproperly$allowBeg(EntityMaid maid) {
        long now = maid.level().getGameTime();
        return SnackRaidBegPolicy.allowBeg(
                SnackRaidData.isActive(maid),
                AngerAttractionPolicy.shouldAllowAttraction(OutOfPoolAngerData.isAngry(maid, now)));
    }
}
