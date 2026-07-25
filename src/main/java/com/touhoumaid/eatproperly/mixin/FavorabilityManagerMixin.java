package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.favorability.FavorabilityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FavorabilityManager.class, remap = false)
public abstract class FavorabilityManagerMixin {
    @Shadow
    private EntityMaid maid;

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
