package com.touhoumaid.eatproperly.effect;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EatProperlyMod.MOD_ID);

    public static final RegistryObject<MobEffect> HAPPINESS = MOB_EFFECTS.register("happiness", HappinessEffect::new);

    private ModEffects() {
    }
}
