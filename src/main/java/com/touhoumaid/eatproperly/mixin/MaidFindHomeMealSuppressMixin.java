package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidFindHomeMealTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.food.PicnicRefill;
import com.touhoumaid.eatproperly.food.PicnicRefillData;
import com.touhoumaid.eatproperly.food.PicnicRefillPolicy;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * N3.10: while a maid has left an empty home-meal mat to fetch a picnic refill without food yet,
 * stop the brain from immediately walking her back to the (still empty) mat.
 */
@Mixin(value = MaidFindHomeMealTask.class, remap = false)
public abstract class MaidFindHomeMealSuppressMixin {
    @Inject(method = "checkExtraStartConditions", at = @At("HEAD"), cancellable = true)
    private void eatproperly$suppressWhileSeekingRefill(
            ServerLevel level, EntityMaid maid, CallbackInfoReturnable<Boolean> cir) {
        boolean seeking = PicnicRefillData.isSeeking(maid);
        boolean carries = PicnicRefill.carriesPoolFood(maid);
        if (PicnicRefillPolicy.shouldSuppressFindHomeMeal(seeking, carries)) {
            cir.setReturnValue(false);
        }
    }
}
