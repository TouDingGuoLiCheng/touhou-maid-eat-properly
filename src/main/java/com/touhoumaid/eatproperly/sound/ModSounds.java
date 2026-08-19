package com.touhoumaid.eatproperly.sound;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, EatProperlyMod.MOD_ID);

    public static final RegistryObject<SoundEvent> SILVER_BELL_CHIME = SOUND_EVENTS.register(
            "silver_bell_chime",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "silver_bell_chime")));

    private ModSounds() {
    }
}
