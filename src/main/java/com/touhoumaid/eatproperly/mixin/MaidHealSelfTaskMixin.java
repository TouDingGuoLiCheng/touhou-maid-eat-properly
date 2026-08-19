package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidHealSelfTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.satiety.HealMealData;
import com.touhoumaid.eatproperly.satiety.HealMealPolicy;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Pace heal-self meals like work/home meals: respect the shared satiety gate and a post-eat cooldown
 * so an injured maid no longer opens the task every ~50 ticks at any satiety.
 */
@Mixin(value = MaidHealSelfTask.class, remap = false)
public abstract class MaidHealSelfTaskMixin {
    @Inject(
            method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private void eatproperly$gateHealSelfMeal(
            ServerLevel level, EntityMaid maid, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        SatietyData.ensureInitialized(maid);
        int satiety = SatietyData.getSatiety(maid);
        int gate = EatProperlyConfig.EAT_WHEN_SATIETY_BELOW.get();
        int cooldown = EatProperlyConfig.HEAL_MEAL_COOLDOWN_TICKS.get();
        long now = level.getGameTime();
        if (!HealMealPolicy.allowsEat(satiety, gate, HealMealData.getLastHealMealTime(maid), now, cooldown)) {
            cir.setReturnValue(false);
        }
    }
}
