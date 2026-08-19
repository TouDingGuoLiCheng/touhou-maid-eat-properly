package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidHomeMealTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidWorkMealTask;
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.satiety.MaidMealHungerPolicy;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When work/home meal cooldown fires but satiety is too high, fake-skip: apply TLM meal
 * cooldown with 0 favor and cancel the task body so picnic scans never see "no edible food"
 * (which would show the empty-meal bubble). Find-picnic / sit is unchanged.
 */
@Mixin(value = {
        MaidHomeMealTask.class,
        MaidWorkMealTask.class
}, remap = false)
public abstract class MaidMealTaskSkipMixin {
    @Inject(
            method = "start(Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;J)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eatproperly$fakeSkipWhenFull(ServerLevel level, EntityMaid maid, long gameTime, CallbackInfo ci) {
        SatietyData.ensureInitialized(maid);
        int satiety = SatietyData.getSatiety(maid);
        int gate = EatProperlyConfig.EAT_WHEN_SATIETY_BELOW.get();
        if (!MaidMealHungerPolicy.shouldFakeSkipMeal(satiety, gate)) {
            return;
        }

        boolean home = (Object) this instanceof MaidHomeMealTask;
        Type type = home ? Type.HOME_MEAL : Type.WORK_MEAL;
        maid.getFavorabilityManager().apply(type, 0);
        ci.cancel();
    }
}
