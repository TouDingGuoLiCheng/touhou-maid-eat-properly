package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidHealSelfMeal;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.satiety.HealMealData;
import com.touhoumaid.eatproperly.satiety.HealMealPolicy;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DefaultMaidHealSelfMeal.class, remap = false)
public abstract class MaidHealSelfMealMixin {
    @Inject(method = "canMaidEat", at = @At("RETURN"), cancellable = true)
    private void eatproperly$requireSatietyGate(
            EntityMaid maid, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        SatietyData.ensureInitialized(maid);
        if (!HealMealPolicy.satietyAllows(
                SatietyData.getSatiety(maid), EatProperlyConfig.EAT_WHEN_SATIETY_BELOW.get())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onMaidEat", at = @At("RETURN"))
    private void eatproperly$stampHealMealCooldown(
            EntityMaid maid, ItemStack stack, InteractionHand hand, CallbackInfo ci) {
        if (maid.level().isClientSide()) {
            return;
        }
        HealMealData.setLastHealMealTime(maid, maid.level().getGameTime());
    }
}
