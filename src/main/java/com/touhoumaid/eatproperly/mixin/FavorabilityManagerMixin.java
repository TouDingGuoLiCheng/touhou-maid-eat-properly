package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.favorability.FavorabilityExtension;
import com.touhoumaid.eatproperly.satiety.HungerBackpackMeal;
import com.touhoumaid.eatproperly.satiety.MaidMealHungerPolicy;
import com.touhoumaid.eatproperly.satiety.MealFavorPolicy;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FavorabilityManager.class, remap = false)
public abstract class FavorabilityManagerMixin {
    @Shadow
    private EntityMaid maid;

    @Shadow
    public abstract void add(int point);

    @Shadow
    public abstract void reduce(int point);

    @Shadow
    private void addCooldown(String type, int tickCount) {
    }

    /**
     * While satiety is below {@code hungryBingeUntilSatiety}, allow WorkMeal/HomeMeal
     * tasks to start despite TLM favor cooldowns so the maid can binge-eat — but only when
     * a real eat is possible. WorkMeal empty inventory must not bypass, or the task spins
     * every ~50 ticks without ever applying cooldown.
     */
    @Inject(method = "canAdd", at = @At("HEAD"), cancellable = true)
    private void eatproperly$bypassMealCooldownWhenHungry(String type, CallbackInfoReturnable<Boolean> cir) {
        if (!MaidMealHungerPolicy.isMealFavorType(type)) {
            return;
        }
        SatietyData.ensureInitialized(this.maid);
        int satiety = SatietyData.getSatiety(this.maid);
        int bingeUntil = EatProperlyConfig.HUNGRY_BINGE_UNTIL_SATIETY.get();
        boolean hasFood = true;
        if (MaidMealHungerPolicy.WORK_MEAL_TYPE.equals(type)) {
            hasFood = HungerBackpackMeal.hasEdibleFood(this.maid);
        }
        if (MaidMealHungerPolicy.shouldBypassMealCooldown(satiety, bingeUntil, hasFood)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * A9: always grant positive work/home meal favor (and refresh cooldown). Satiety is
     * applied before {@code apply} in {@code onMaidEat}, so the hungry {@code canAdd}
     * bypass alone is not enough for the second binge eat.
     */
    @Inject(
            method = "apply(Lcom/github/tartaricacid/touhoulittlemaid/entity/favorability/Type;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eatproperly$forceGrantPositiveMealFavor(Type type, int point, CallbackInfo ci) {
        if (!MealFavorPolicy.shouldForceGrantMealFavor(type.getTypeName(), point)) {
            return;
        }
        if (type.isReduce()) {
            this.reduce(point);
        } else {
            this.add(point);
        }
        this.addCooldown(type.getTypeName(), type.getCooldown());
        ci.cancel();
    }

    @Inject(method = "getLevel(I)I", at = @At("HEAD"), cancellable = true)
    private void eatproperly$getLevel(int favorability, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(FavorabilityExtension.getLevel(favorability));
    }

    @Inject(method = "getPointByLevel", at = @At("HEAD"), cancellable = true)
    private void eatproperly$getPointByLevel(int level, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(FavorabilityExtension.getPointByLevel(level));
    }

    @Inject(method = "getHealthByLevel", at = @At("HEAD"), cancellable = true)
    private void eatproperly$getHealthByLevel(int level, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(FavorabilityExtension.getHealthByLevel(level));
    }

    @Inject(method = "getAttackByLevel", at = @At("HEAD"), cancellable = true)
    private void eatproperly$getAttackByLevel(int level, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(FavorabilityExtension.getAttackByLevel(level));
    }

    @Inject(method = "getLevelPercent", at = @At("HEAD"), cancellable = true)
    private void eatproperly$getLevelPercent(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(FavorabilityExtension.getLevelPercent(this.maid.getFavorability()));
    }

    @Inject(method = "nextLevelPoint", at = @At("HEAD"), cancellable = true)
    private void eatproperly$nextLevelPoint(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(FavorabilityExtension.nextLevelPoint(this.maid.getFavorability()));
    }

    @ModifyConstant(method = "add", constant = @Constant(intValue = 384))
    private int eatproperly$modifyAddCap(int original) {
        return FavorabilityExtension.getMaxPoint();
    }

    @ModifyConstant(method = "reduce", constant = @Constant(intValue = 384))
    private int eatproperly$modifyReduceCap(int original) {
        return FavorabilityExtension.getMaxPoint();
    }

    @ModifyConstant(method = "reduceWithoutLevel", constant = @Constant(intValue = 384))
    private int eatproperly$modifyReduceWithoutLevelCap(int original) {
        return FavorabilityExtension.getMaxPoint();
    }

    @ModifyConstant(method = "max", constant = @Constant(intValue = 384))
    private int eatproperly$modifyMaxFill(int original) {
        return FavorabilityExtension.getMaxPoint();
    }
}
