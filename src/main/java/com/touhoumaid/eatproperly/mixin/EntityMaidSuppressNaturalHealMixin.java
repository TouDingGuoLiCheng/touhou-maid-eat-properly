package com.touhoumaid.eatproperly.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppress TLM passive random heal; maid regen is driven by {@link com.touhoumaid.eatproperly.satiety.MaidFoodData}.
 */
@Mixin(value = com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid.class, remap = false)
public abstract class EntityMaidSuppressNaturalHealMixin {
    @Inject(method = "randomRestoreHealth", at = @At("HEAD"), cancellable = true)
    private void eatproperly$suppressTlmNaturalHeal(CallbackInfo ci) {
        ci.cancel();
    }
}
