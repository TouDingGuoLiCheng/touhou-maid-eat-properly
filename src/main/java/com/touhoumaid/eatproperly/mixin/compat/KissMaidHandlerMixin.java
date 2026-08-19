package com.touhoumaid.eatproperly.mixin.compat;

import com.touhoumaid.eatproperly.compat.TouhouMaidAffectionCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * When favor exceeds vanilla level 3, Affection's tiered kiss cooldown / Maids Prayer amplifier
 * fall through to the level-0 branch. Clamp extended levels to tier 3 (best vanilla tier).
 */
@Mixin(targets = "com.github.touhoumaidaffection.handler.KissMaidHandler", remap = false)
public class KissMaidHandlerMixin {
    @ModifyVariable(method = "getCooldownForLevel", at = @At("HEAD"), argsOnly = true)
    private static int eatproperly$clampCooldownTier(int level) {
        return TouhouMaidAffectionCompat.clampAffectionTier(level);
    }

    @ModifyVariable(method = "getAmplifierForLevel", at = @At("HEAD"), argsOnly = true)
    private static int eatproperly$clampAmplifierTier(int level) {
        return TouhouMaidAffectionCompat.clampAffectionTier(level);
    }
}
