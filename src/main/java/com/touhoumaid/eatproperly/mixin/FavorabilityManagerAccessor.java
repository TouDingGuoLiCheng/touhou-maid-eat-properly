package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = FavorabilityManager.class, remap = false)
public interface FavorabilityManagerAccessor {
    @Accessor("counter")
    Map<String, FavorabilityManager.Time> eatproperly$getCounter();
}
